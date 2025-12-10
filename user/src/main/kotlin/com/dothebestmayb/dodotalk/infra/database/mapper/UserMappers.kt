package com.dothebestmayb.dodotalk.infra.database.mapper

import com.dothebestmayb.dodotalk.domain.model.User
import com.dothebestmayb.dodotalk.infra.database.entities.UserEntity

fun UserEntity.toUser(): User {
    return User(
        id = id!!,
        username = username,
        email = email,
        hasEmailVerified = hasVerifiedEmail,
    )
}