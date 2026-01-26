package com.dothebestmayb.chat.service

import com.dothebestmayb.chat.domain.constant.ChatConstants
import com.dothebestmayb.chat.domain.exception.ChatParticipantNotFoundException
import com.dothebestmayb.chat.domain.exception.InvalidChatSizeException
import com.dothebestmayb.chat.domain.models.Chat
import com.dothebestmayb.chat.infra.database.entities.ChatEntity
import com.dothebestmayb.chat.infra.database.mappers.toChat
import com.dothebestmayb.chat.infra.database.repositories.ChatParticipantRepository
import com.dothebestmayb.chat.infra.database.repositories.ChatRepository
import com.dothebestmayb.dodotalk.domain.type.UserId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatParticipantRepository: ChatParticipantRepository,
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
}