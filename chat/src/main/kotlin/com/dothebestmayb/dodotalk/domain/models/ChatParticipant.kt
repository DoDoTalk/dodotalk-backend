package com.dothebestmayb.dodotalk.domain.models

import com.dothebestmayb.dodotalk.domain.type.UserId

data class ChatParticipant(
    val userId: UserId,
    val username: String,
    val email: String,
    val profilePictureUrl: String?,
)
