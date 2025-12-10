package com.dothebestmayb.dodotalk.infra.security

import com.dothebestmayb.dodotalk.domain.exception.EncodePasswordException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordEncoder {

    private val bcrypt = BCryptPasswordEncoder()

    /**
     * rawPassword가 null이 아니면 encode 함수는 null을 반환하지 않지만
     * !! 대신 안전하게 exception을 던지도록 처리함
     */
    fun encode(rawPassword: String): String {
        return bcrypt.encode(rawPassword) ?: throw EncodePasswordException()
    }

    fun matches(rawPassword: String, hashedPassword: String): Boolean {
        return bcrypt.matches(rawPassword, hashedPassword)
    }
}