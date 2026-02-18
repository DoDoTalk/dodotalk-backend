package com.dothebestmayb.chat.infra.database.mappers

import com.dothebestmayb.chat.domain.models.Chat
import com.dothebestmayb.chat.domain.models.ChatMessage
import com.dothebestmayb.chat.domain.models.ChatParticipant
import com.dothebestmayb.chat.infra.database.entities.ChatEntity
import com.dothebestmayb.chat.infra.database.entities.ChatParticipantEntity

fun ChatEntity.toChat(lastMessage: ChatMessage? = null): Chat {
    return Chat(
        id = id!!,
        participants = participants.map {
            it.toChatParticipant()
        }.toSet(),
        lastMessage = lastMessage,
        creator = creator.toChatParticipant(),
        lastActivityAt = lastMessage?.createdAt ?: createdAt,
        createdAt = createdAt,
    )
}

fun ChatParticipantEntity.toChatParticipant(): ChatParticipant {
    return ChatParticipant(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl,
    )
}

fun ChatParticipant.toChatParticipantEntity(): ChatParticipantEntity {
    return ChatParticipantEntity(
        userId = userId,
        username = username,
        email = email,
        profilePictureUrl = profilePictureUrl,
    )
}