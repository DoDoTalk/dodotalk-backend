package com.dothebestmayb.dodotalk.domain.event

import com.dothebestmayb.dodotalk.domain.type.ChatId
import com.dothebestmayb.dodotalk.domain.type.UserId

data class ChatParticipantsJoinedEvent(
    val chatId: ChatId,
    val userIds: Set<UserId>
)
