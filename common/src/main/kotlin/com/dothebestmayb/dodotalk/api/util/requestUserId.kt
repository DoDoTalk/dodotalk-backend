package com.dothebestmayb.dodotalk.api.util

import com.dothebestmayb.dodotalk.domain.exception.UnauthorizedException
import com.dothebestmayb.dodotalk.domain.type.UserId
import org.springframework.security.core.context.SecurityContextHolder

val requestUserId: UserId
    get() = SecurityContextHolder.getContext().authentication?.principal as? UserId
        ?: throw UnauthorizedException()