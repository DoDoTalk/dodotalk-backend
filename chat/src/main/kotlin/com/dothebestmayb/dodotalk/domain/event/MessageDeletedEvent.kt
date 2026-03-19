package com.dothebestmayb.dodotalk.domain.event

import com.dothebestmayb.dodotalk.domain.type.ChatId
import com.dothebestmayb.dodotalk.domain.type.ChatMessageId

data class MessageDeletedEvent(
    val chatId: ChatId,
    val messageId: ChatMessageId,
)
