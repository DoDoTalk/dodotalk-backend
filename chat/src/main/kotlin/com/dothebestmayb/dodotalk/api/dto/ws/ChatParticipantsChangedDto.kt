package com.dothebestmayb.dodotalk.api.dto.ws

import com.dothebestmayb.dodotalk.domain.type.ChatId

data class ChatParticipantsChangedDto(
    val chatId: ChatId,
)
