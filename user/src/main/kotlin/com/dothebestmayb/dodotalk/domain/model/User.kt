package com.dothebestmayb.dodotalk.domain.model

import java.util.UUID

/**
 * typealias를 사용하면 USER ID를 UUID가 아닌 다른 타입으로 변환할 때 이 타입만 수정하면 된다.
 */
typealias UserId = UUID

// password는 data layer에서 필요하므로, domain layer에서는 선언하지 않음
data class User(
    val id: UserId,
    val username: String,
    val email: String,
    val hasEmailVerified: Boolean,
)
