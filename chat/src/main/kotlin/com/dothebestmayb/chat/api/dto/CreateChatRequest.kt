package com.dothebestmayb.chat.api.dto

import com.dothebestmayb.chat.domain.constant.ChatConstants
import com.dothebestmayb.dodotalk.domain.type.UserId
import jakarta.validation.constraints.Size

data class CreateChatRequest(
    @field:Size(
        min = ChatConstants.MIN_CHAT_PARTICIPANTS,
        message = "Chats must have at least ${ChatConstants.MIN_CHAT_PARTICIPANTS} unique participants"
    )
    val otherUserIds: List<UserId>,
)
