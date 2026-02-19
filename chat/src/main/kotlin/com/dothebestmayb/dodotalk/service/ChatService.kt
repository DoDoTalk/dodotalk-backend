package com.dothebestmayb.dodotalk.service

import com.dothebestmayb.dodotalk.domain.constant.ChatConstants
import com.dothebestmayb.dodotalk.domain.exception.ChatParticipantNotFoundException
import com.dothebestmayb.dodotalk.domain.exception.InvalidChatSizeException
import com.dothebestmayb.dodotalk.domain.models.Chat
import com.dothebestmayb.dodotalk.infra.database.entities.ChatEntity
import com.dothebestmayb.dodotalk.infra.database.mappers.toChat
import com.dothebestmayb.dodotalk.infra.database.repositories.ChatParticipantRepository
import com.dothebestmayb.dodotalk.infra.database.repositories.ChatRepository
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