package com.dothebestmayb.dodotalk.service

import com.dothebestmayb.dodotalk.domain.events.user.UserEvent
import com.dothebestmayb.dodotalk.domain.exception.InvalidCredentialsException
import com.dothebestmayb.dodotalk.domain.exception.InvalidTokenException
import com.dothebestmayb.dodotalk.domain.exception.SamePasswordException
import com.dothebestmayb.dodotalk.domain.exception.UserNotFoundException
import com.dothebestmayb.dodotalk.domain.type.UserId
import com.dothebestmayb.dodotalk.infra.database.entities.PasswordResetTokenEntity
import com.dothebestmayb.dodotalk.infra.database.repositories.PasswordResetTokenRepository
import com.dothebestmayb.dodotalk.infra.database.repositories.RefreshTokenRepository
import com.dothebestmayb.dodotalk.infra.database.repositories.UserRepository
import com.dothebestmayb.dodotalk.infra.message_queue.EventPublisher
import com.dothebestmayb.dodotalk.infra.security.PasswordEncoder
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val passwordResetTokenRepository: PasswordResetTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    @param:Value("\${dodotalk.email.reset-password.expiry-minutes}")
    private val expiryMinutes: Long,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val eventPublisher: EventPublisher,
) {
    @Transactional
    fun requestPasswordReset(email: String) {
        val user = userRepository.findByEmail(email) ?: return

        passwordResetTokenRepository.invalidateActiveTokensForUser(user)

        val token = PasswordResetTokenEntity(
            user = user,
            expiresAt = Instant.now().plus(expiryMinutes, ChronoUnit.MINUTES)
        )
        passwordResetTokenRepository.save(token)

        eventPublisher.publish(
            event = UserEvent.RequestResetPassword(
                userId = user.id!!,
                email = user.email,
                username = user.username,
                passwordResetToken = token.token,
                expiresInMinutes = expiryMinutes,
            )
        )
    }

    @Transactional
    fun resetPassword(token: String, newPassword: String) {
        val resetToken = passwordResetTokenRepository.findByToken(token)
            ?: throw InvalidTokenException("Invalid password reset token")

        if (resetToken.isUsed) {
            throw InvalidTokenException("Email verification token is already used.")
        }

        if (resetToken.isExpired) {
            throw InvalidTokenException("Email verification token has already expired.")
        }

        val user = resetToken.user

        if (passwordEncoder.matches(newPassword, user.hashedPassword)) {
            throw SamePasswordException()
        }
        val hashedNewPassword = passwordEncoder.encode(newPassword)
        userRepository.save(
            user.apply {
                this.hashedPassword = hashedNewPassword
            }
        )

        passwordResetTokenRepository.save(
            resetToken.apply {
                this.usedAt = Instant.now()
            }
        )

        refreshTokenRepository.deleteByUserId(user.id!!)
    }

    @Transactional
    fun changePassword(
        userId: UserId,
        oldPassword: String,
        newPassword: String,
    ) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException()

        if (!passwordEncoder.matches(oldPassword, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }

        if (oldPassword == newPassword) {
            throw SamePasswordException()
        }

        refreshTokenRepository.deleteByUserId(user.id!!)

        val newHashedPassword = passwordEncoder.encode(newPassword)
        userRepository.save(
            user.apply {
                this.hashedPassword = newHashedPassword
            }
        )
    }

    @Scheduled(cron = "0 0 3 * * *", zone = "\${dodotalk.scheduling.time-zone}")
    fun cleanupExpiredTokens() {
        passwordResetTokenRepository.deleteByExpiresAtLessThan(
            Instant.now()
        )
    }
}