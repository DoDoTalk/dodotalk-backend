package com.dothebestmayb.chat.api.dto

import com.dothebestmayb.dodotalk.domain.type.UserId

data class ChatParticipantDto(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?,
)
