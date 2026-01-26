package com.dothebestmayb.chat.domain.exception

import com.dothebestmayb.dodotalk.domain.type.UserId

class ChatParticipantNotFoundException(
    private val id: UserId,
): RuntimeException(
    "The chat participant with the ID $id was not found"
)