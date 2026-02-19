package com.dothebestmayb.dodotalk.api.dto

import com.dothebestmayb.dodotalk.domain.type.UserId
import jakarta.validation.constraints.Size

data class AddParticipantToChatDto(
    @field:Size(min = 1)
    val userIds: List<UserId>,
)
