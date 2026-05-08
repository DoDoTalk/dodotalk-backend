package com.dothebestmayb.dodotalk.api.websocket

import com.dothebestmayb.dodotalk.api.dto.ws.ChatParticipantsChangedDto
import com.dothebestmayb.dodotalk.api.dto.ws.DeleteMessageDto
import com.dothebestmayb.dodotalk.api.dto.ws.ErrorDto
import com.dothebestmayb.dodotalk.api.dto.ws.IncomingWebSocketMessage
import com.dothebestmayb.dodotalk.api.dto.ws.IncomingWebSocketMessageType
import com.dothebestmayb.dodotalk.api.dto.ws.OutgoingWebSocketMessage
import com.dothebestmayb.dodotalk.api.dto.ws.OutgoingWebSocketMessageType
import com.dothebestmayb.dodotalk.api.dto.ws.SendMessageDto
import com.dothebestmayb.dodotalk.api.mappers.toChatMessageDto
import com.dothebestmayb.dodotalk.domain.event.ChatParticipantLeftEvent
import com.dothebestmayb.dodotalk.domain.event.ChatParticipantsJoinedEvent
import com.dothebestmayb.dodotalk.domain.event.MessageDeletedEvent
import com.dothebestmayb.dodotalk.domain.type.ChatId
import com.dothebestmayb.dodotalk.domain.type.UserId
import com.dothebestmayb.dodotalk.service.ChatMessageService
import com.dothebestmayb.dodotalk.service.ChatService
import com.dothebestmayb.dodotalk.service.JwtService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import tools.jackson.core.JacksonException
import tools.jackson.databind.ObjectMapper
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.read
import kotlin.concurrent.write

@Component
class ChatWebSocketHandler(
    private val chatMessageService: ChatMessageService,
    private val objectMapper: ObjectMapper,
    private val chatService: ChatService,
    private val jwtService: JwtService,
) : TextWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(javaClass)

    // ConcurrentHashMap은 각각에 대한 동시성만 보장한다. 4개의 race condition을 해결하기 위해 Lock을 사용함
    private val connectionLock = ReentrantReadWriteLock()

    private val sessions = ConcurrentHashMap<String, UserSession>()
    private val userToSessions = ConcurrentHashMap<UserId, MutableSet<String>>() // value : Set<SessionId>
    private val userChatIds = ConcurrentHashMap<UserId, MutableSet<ChatId>>()
    private val chatToSessions = ConcurrentHashMap<ChatId, MutableSet<String>>() // value : Set<SessionId>

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val authHeader = session
            .handshakeHeaders
            .getFirst(HttpHeaders.AUTHORIZATION)
            ?: run {
                logger.warn("Session ${session.id} was closed due to missing Authorization header")
                session.close(CloseStatus.SERVER_ERROR.withReason("Authentication failed"))
                return
            }

        val userId = jwtService.getUserIdFromToken(authHeader)

        val userSession = UserSession(
            userId = userId,
            session = session,
        )

        connectionLock.write {
            sessions[session.id] = userSession

            userToSessions.compute(userId) { _, existingSessions ->
                (existingSessions ?: mutableSetOf()).apply {
                    add(session.id)
                }
            }

            val chatIds = userChatIds.computeIfAbsent(userId) {
                val chatIds = chatService.findChatByUser(userId).map { it.id }
                ConcurrentHashMap.newKeySet<ChatId>().apply {
                    addAll(chatIds)
                }
            }

            chatIds.forEach { chatId ->
                chatToSessions.compute(chatId) { _, sessions ->
                    (sessions ?: mutableSetOf()).apply {
                        add(session.id)
                    }
                }
            }
        }

        logger.info("Websocket connection established for user $userId")
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.debug("Received message ${message.payload}")

        val userSession = connectionLock.read {
            sessions[session.id] ?: return
        }

        try {
            val webSocketMessage = objectMapper.readValue(
                message.payload, // 수신한 메시지의 payload
                IncomingWebSocketMessage::class.java
            )

            when (webSocketMessage.type) {
                IncomingWebSocketMessageType.NEW_MESSAGE -> {
                    val dto = objectMapper.readValue(
                        webSocketMessage.payload, // 가변적인 메시지에 따라 파싱하기 위해 내부적으로 명명한 payload
                        SendMessageDto::class.java
                    )
                    handleSendMessage(
                        dto = dto,
                        senderId = userSession.userId,
                    )
                }
            }
        } catch (e: JacksonException) {
            logger.warn("Could not parse message ${message.payload}", e)
            sendError(
                session = userSession.session,
                error = ErrorDto(
                    code = "INVALID_JSON",
                    message = "Incoming JSON or UUID is invalid"
                )
            )
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onDeleteMessage(event: MessageDeletedEvent) {
        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.MESSAGE_DELETED,
                payload = objectMapper.writeValueAsString(
                    DeleteMessageDto(
                        chatId = event.chatId,
                        messageId = event.messageId,
                    )
                )
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onJoinChat(event: ChatParticipantsJoinedEvent) {
        connectionLock.write {
            val joiningSessionsIds = mutableSetOf<String>()

            event.userIds.forEach { userId ->
                userChatIds.compute(userId) { _, chatIds ->
                    (chatIds ?: mutableSetOf()).apply {
                        add(event.chatId)
                    }
                }

                userToSessions[userId]?.let { sessionIds ->
                    joiningSessionsIds.addAll(sessionIds)
                }
            }

            if (joiningSessionsIds.isNotEmpty()) {
                chatToSessions.compute(event.chatId) { _, sessions ->
                    (sessions ?: mutableSetOf()).apply { addAll(joiningSessionsIds) }
                }
            }
        }

        // client에게 어떤 event가 있는지 알려주는 것이 아니라, 대화 내역 갱신이 필요함을 알린다.
        // 이렇게 함으로써, 채팅 참여, 삭제, 수정 등에 대해 분기 처리 없이 "갱신" 한 개로 처리 가능하다.
        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.CHAT_PARTICIPANTS_CHANGED,
                payload = objectMapper.writeValueAsString(
                    ChatParticipantsChangedDto(
                        chatId = event.chatId,
                    )
                )
            )
        )
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun onLeftChat(event: ChatParticipantLeftEvent) {
        connectionLock.write {
            userChatIds.compute(event.userId) { _, chatIds ->
                chatIds
                    ?.apply { remove(event.chatId) }
                    ?.takeIf { it.isNotEmpty() }
            }

            val leavingSessionIds = userToSessions[event.userId] ?: emptySet()

            chatToSessions.compute(event.chatId) { _, sessions ->
                sessions
                    ?.apply { removeAll(leavingSessionIds) }
                    ?.takeIf { it.isNotEmpty() }
            }
        }

        // client에게 어떤 event가 있는지 알려주는 것이 아니라, 대화 내역 갱신이 필요함을 알린다.
        // 이렇게 함으로써, 채팅 참여, 삭제, 수정 등에 대해 분기 처리 없이 "갱신" 한 개로 처리 가능하다.
        broadcastToChat(
            chatId = event.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.CHAT_PARTICIPANTS_CHANGED,
                payload = objectMapper.writeValueAsString(
                    ChatParticipantsChangedDto(
                        chatId = event.chatId,
                    )
                )
            )
        )
    }

    private fun sendError(
        session: WebSocketSession,
        error: ErrorDto
    ) {
        val webSocketMessage = objectMapper.writeValueAsString(
            OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.ERROR,
                payload = objectMapper.writeValueAsString(error)
            )
        )

        try {
            session.sendMessage(TextMessage(webSocketMessage))
        } catch(e: Exception) {
            logger.warn("Couldn't send error message", e)
        }
    }


    private fun broadcastToChat(
        chatId: ChatId,
        message: OutgoingWebSocketMessage
    ) {
        val chatSessions = connectionLock.read {
            chatToSessions[chatId]?.toList() ?: emptyList()
        }

        chatSessions.forEach { sessionId ->
            val userSession = connectionLock.read {
                sessions[sessionId]
            } ?: return@forEach

            sendToUser(
                userId = userSession.userId,
                message = message,
            )
        }
    }


    private fun handleSendMessage(
        dto: SendMessageDto,
        senderId: UserId,
    ) {
        val userChatIds = connectionLock.read { this@ChatWebSocketHandler.userChatIds[senderId] } ?: return

        if (dto.chatId !in userChatIds) {
            return
        }

        val savedMessage = chatMessageService.sendMessage(
            chatId = dto.chatId,
            senderId = senderId,
            content = dto.content,
            messageId = dto.messageId,
        )

        broadcastToChat(
            chatId = dto.chatId,
            message = OutgoingWebSocketMessage(
                type = OutgoingWebSocketMessageType.NEW_MESSAGE,
                payload = objectMapper.writeValueAsString(
                    savedMessage.toChatMessageDto()
                )
            )
        )
    }

    private fun sendToUser(userId: UserId, message: OutgoingWebSocketMessage) {
        val userSessions = connectionLock.read {
            userToSessions[userId] ?: emptySet()
        }
        userSessions.forEach { sessionId ->
            val userSession = connectionLock.read {
                sessions[sessionId] ?: return@forEach
            }
            if (userSession.session.isOpen) {
                try {
                    val messageJson = objectMapper.writeValueAsString(message)
                    userSession.session.sendMessage(TextMessage(messageJson))
                    logger.debug("Sent message to user {}: {}", userId, messageJson)
                } catch (e: Exception) {
                    logger.error("Error while sending message to $userId", e)
                }
            }
        }
    }

    private data class UserSession(
        val userId: UserId,
        val session: WebSocketSession,
    )
}