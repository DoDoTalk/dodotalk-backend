package com.dothebestmayb.dodotalk.domain.event

import com.dothebestmayb.dodotalk.domain.type.ChatId
import com.dothebestmayb.dodotalk.domain.type.UserId

data class ChatParticipantLeftEvent(
    val chatId: ChatId,
    val userId: UserId
)
