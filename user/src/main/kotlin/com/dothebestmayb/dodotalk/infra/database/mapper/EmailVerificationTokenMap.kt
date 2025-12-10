package com.dothebestmayb.dodotalk.infra.database.mapper

import com.dothebestmayb.dodotalk.domain.model.EmailVerificationToken
import com.dothebestmayb.dodotalk.infra.database.entities.EmailVerificationTokenEntity

fun EmailVerificationTokenEntity.toEmailVerificationToken(): EmailVerificationToken {
    return EmailVerificationToken(
        id = id,
        token = token,
        user = user.toUser(),
    )
}