package com.dothebestmayb.dodotalk.domain.model

import com.dothebestmayb.dodotalk.domain.type.UserId

// password는 data layer에서 필요하므로, domain layer에서는 선언하지 않음
data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val hasEmailVerified: Boolean,
)
