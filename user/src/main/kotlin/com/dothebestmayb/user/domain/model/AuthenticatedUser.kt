package com.dothebestmayb.user.domain.model

/**
 * 로그인한 사용자 정보
 */
data class AuthenticatedUser(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
)
