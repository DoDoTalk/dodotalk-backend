package com.dothebestmayb.dodotalk.service

import com.dothebestmayb.dodotalk.domain.exception.ChatNotFoundException
import com.dothebestmayb.dodotalk.domain.exception.ChatParticipantNotFoundException
import com.dothebestmayb.dodotalk.domain.exception.ForbiddenException
import com.dothebestmayb.dodotalk.domain.exception.MessageNotFoundException
import com.dothebestmayb.dodotalk.domain.models.ChatMessage
import com.dothebestmayb.dodotalk.domain.type.ChatId
import com.dothebestmayb.dodotalk.domain.type.ChatMessageId
import com.dothebestmayb.dodotalk.domain.type.UserId
import com.dothebestmayb.dodotalk.infra.database.entities.ChatMessageEntity
import com.dothebestmayb.dodotalk.infra.database.mappers.toChatMessage
import com.dothebestmayb.dodotalk.infra.database.repositories.ChatMessageRepository
import com.dothebestmayb.dodotalk.infra.database.repositories.ChatParticipantRepository
import com.dothebestmayb.dodotalk.infra.database.repositories.ChatRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatMessageService(
    private val chatRepository: ChatRepository,
    private val chatMessageRepository: ChatMessageRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
) {

    /**
     * @param messageId 메시지를 보낸 사람은 broadcast에 의해 자신도 메시지를 받게 되는데
     *  받은 메시지가 보낸 메시지와 동일한지 MessageId를 비교하여 메시지 전송에 성공했는지 비교함
     *  따라서 messageId를 server에서 auto generate 하지 않고 client에서 생성하도록 구성함
     */
    @Transactional
    fun sendMessage(
        chatId: ChatId,
        senderId: UserId,
        content: String,
        messageId: ChatMessageId? = null,
    ): ChatMessage {
        val chat = chatRepository.findChatById(chatId, senderId)
            ?: throw ChatNotFoundException()
        val sender = chatParticipantRepository.findByIdOrNull(senderId)
            ?: throw ChatParticipantNotFoundException(senderId)

        val savedMessage = chatMessageRepository.save(
            ChatMessageEntity(
                id = messageId,
                content = content.trim(),
                chatId = chatId,
                chat = chat,
                sender = sender,
            )
        )

        return savedMessage.toChatMessage()
    }

    @Transactional
    fun deleteMessage(
        messageId: ChatMessageId,
        requestUserId: UserId,
    ) {
        val message = chatMessageRepository.findByIdOrNull(messageId)
            ?: throw MessageNotFoundException(messageId)

        if (message.sender.userId != requestUserId) {
            throw ForbiddenException()
        }

        chatMessageRepository.delete(message)
    }
}