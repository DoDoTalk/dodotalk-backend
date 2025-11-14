package com.dothebestmayb.dodotalk.infra.database.repositories

import com.dothebestmayb.dodotalk.infra.database.entities.EmailVerificationTokenEntity
import com.dothebestmayb.dodotalk.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface EmailVerificationTokenRepository: JpaRepository<EmailVerificationTokenEntity, Long> {
    fun findByToken(token: String): EmailVerificationTokenEntity?
    fun deleteByExpiresAtLessThan(now: Instant)
    fun findByUserAndUsedAtIsNull(user: UserEntity): List<EmailVerificationTokenEntity>
}