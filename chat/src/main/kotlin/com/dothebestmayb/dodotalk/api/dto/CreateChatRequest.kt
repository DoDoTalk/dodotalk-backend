package com.dothebestmayb.dodotalk.api.dto

import com.dothebestmayb.dodotalk.domain.constant.ChatConstants
import com.dothebestmayb.dodotalk.domain.type.UserId
import jakarta.validation.constraints.Size

data class CreateChatRequest(
    @field:Size(
        min = ChatConstants.MIN_CHAT_PARTICIPANTS,
        message = "Chats must have at least ${ChatConstants.MIN_CHAT_PARTICIPANTS} unique participants"
    )
    val otherUserIds: List<UserId>,
)
