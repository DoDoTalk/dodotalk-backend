package com.dothebestmayb.dodotalk.api.dto

import com.dothebestmayb.dodotalk.domain.type.UserId

data class UserDto(
    val id: UserId,
    val email: String,
    val username: String,
    val hasVerifiedEmail: Boolean,
)
