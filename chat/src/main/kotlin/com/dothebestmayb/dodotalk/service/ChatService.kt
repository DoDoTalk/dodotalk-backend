package com.dothebestmayb.dodotalk.service

import com.dothebestmayb.dodotalk.domain.constant.ChatConstants
import com.dothebestmayb.dodotalk.domain.exception.ChatNotFoundException
import com.dothebestmayb.dodotalk.domain.exception.ChatParticipantNotFoundException
import com.dothebestmayb.dodotalk.domain.exception.ForbiddenException
import com.dothebestmayb.dodotalk.domain.exception.InvalidChatSizeException
import com.dothebestmayb.dodotalk.domain.models.Chat
import com.dothebestmayb.dodotalk.domain.models.ChatMessage
import com.dothebestmayb.dodotalk.domain.type.ChatId
import com.dothebestmayb.dodotalk.infra.database.entities.ChatEntity
import com.dothebestmayb.dodotalk.infra.database.mappers.toChat
import com.dothebestmayb.dodotalk.infra.database.repositories.ChatParticipantRepository
import com.dothebestmayb.dodotalk.infra.database.repositories.ChatRepository
import com.dothebestmayb.dodotalk.domain.type.UserId
import com.dothebestmayb.dodotalk.infra.database.mappers.toChatMessage
import com.dothebestmayb.dodotalk.infra.database.repositories.ChatMessageRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
    private val chatMessageRepository: ChatMessageRepository,
) {

    @Transactional
    fun createChat(
        creatorId: UserId,
        otherUserIds: Set<UserId>,
    ): Chat {
        val allParticipants = chatParticipantRepository.findByUserIdIn(
            userIds = otherUserIds + creatorId,
        )

        val creator = chatParticipantRepository.findByIdOrNull(creatorId)
            ?: throw ChatParticipantNotFoundException(creatorId)

        if (allParticipants.size < ChatConstants.MIN_CHAT_PARTICIPANTS || allParticipants.size > ChatConstants.MAX_CHAT_PARTICIPANTS) {
            throw InvalidChatSizeException()
        }

        return chatRepository.save(
            ChatEntity(
                creator = creator,
                participants = allParticipants,
            )
        ).toChat(lastMessage = null)
    }

    @Transactional
    fun addParticipantsToChat(
        requestUserId: UserId,
        chatId: ChatId,
        userIds: Set<UserId>,
    ): Chat {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()

        val isRequestingUserInChat = chat.participants.any {
            it.userId == requestUserId
        }
        if (!isRequestingUserInChat) {
            throw ForbiddenException()
        }

        val users = userIds.map { userId ->
            chatParticipantRepository.findByIdOrNull(userId)
                ?: throw ChatParticipantNotFoundException(userId)
        }

        val lastMessage = lastMessageForChat(chatId)
        val updatedChat = chatRepository.save(
            chat.apply {
                this.participants = chat.participants + users
            }
        ).toChat(lastMessage)

        return updatedChat
    }

    @Transactional
    fun removeParticipantFromChat(
        chatId: ChatId,
        userId: UserId,
    ) {
        val chat = chatRepository.findByIdOrNull(chatId)
            ?: throw ChatNotFoundException()
        val participant = chat.participants.find { it.userId == userId }
            ?: throw ChatParticipantNotFoundException(userId)

        // userId가 채팅 방의 유일한 사용자면 채팅 제거하기
        val newParticipantSize = chat.participants.size - 1
        if (newParticipantSize == 0) {
            chatRepository.deleteById(chatId)
            // 채팅방과 관련된 데이터들은 여기서 직접 제거하지 않고
            // sql cascade를 이용해 삭제되도록 각 Entity에 annotation을 설정함
            // annotation만 설정하면 hibernate가 자동으로 지워주지 않음
            // DB에서 직접 설정해야 하며, supabase는 Foreign key action을 설정하면 됨
            return
        }

        chatRepository.save(
            chat.apply {
                this.participants = chat.participants - participant
            }
        )
    }

    private fun lastMessageForChat(chatId: ChatId): ChatMessage? {
        return chatMessageRepository
            .findLatestMessageByChatIds(setOf(chatId))
            .firstOrNull()
            ?.toChatMessage()
    }
}