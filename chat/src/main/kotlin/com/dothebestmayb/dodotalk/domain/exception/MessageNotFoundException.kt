package com.dothebestmayb.dodotalk.domain.exception

import com.dothebestmayb.dodotalk.domain.type.ChatMessageId

class MessageNotFoundException(
    private val id: ChatMessageId
) : RuntimeException(
    "Message with ID $id not found"
)