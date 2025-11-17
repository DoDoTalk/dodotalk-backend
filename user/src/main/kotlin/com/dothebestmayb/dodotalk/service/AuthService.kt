package com.dothebestmayb.dodotalk.service

import com.dothebestmayb.dodotalk.domain.exception.EmailNotVerifiedException
import com.dothebestmayb.dodotalk.domain.exception.InvalidCredentialsException
import com.dothebestmayb.dodotalk.domain.exception.InvalidTokenException
import com.dothebestmayb.dodotalk.domain.exception.UserAlreadyExistsException
import com.dothebestmayb.dodotalk.domain.exception.UserNotFoundException
import com.dothebestmayb.dodotalk.domain.model.AuthenticatedUser
import com.dothebestmayb.dodotalk.domain.model.User
import com.dothebestmayb.dodotalk.domain.model.UserId
import com.dothebestmayb.dodotalk.infra.database.entities.RefreshTokenEntity
import com.dothebestmayb.dodotalk.infra.database.entities.UserEntity
import com.dothebestmayb.dodotalk.infra.database.mapper.toUser
import com.dothebestmayb.dodotalk.infra.database.repositories.RefreshTokenRepository
import com.dothebestmayb.dodotalk.infra.database.repositories.UserRepository
import com.dothebestmayb.dodotalk.infra.security.PasswordEncoder
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val emailVerificationService: EmailVerificationService,
) {

    @Transactional
    fun register(email: String, username: String, password: String): User {
        val trimmedEmail = email.trim()

        val user = userRepository.findByEmailOrUsername(
            email = trimmedEmail,
            username = username.trim(),
        )
        if (user != null) {
            throw UserAlreadyExistsException()
        }

        val savedUser = userRepository.saveAndFlush(
            UserEntity(
                email = trimmedEmail,
                username = username.trim(),
                hashedPassword = passwordEncoder.encode(password),
            )
        ).toUser()

        val token = emailVerificationService.createVerificationToken(trimmedEmail)

        return savedUser
    }

    fun login(
        email: String,
        password: String,
    ): AuthenticatedUser {
        val user = userRepository.findByEmail(email.trim())

        val passwordHash = user?.hashedPassword ?: DUMMY_PASSWORD_HASH
        val passwordMatches = passwordEncoder.matches(password, passwordHash)

        if (user == null || !passwordMatches) {
            throw InvalidCredentialsException()
        }

        if (!user.hasVerifiedEmail) {
            throw EmailNotVerifiedException()
        }

        return user.id?.let { userId ->
            val accessToken = jwtService.generateAccessToken(userId)
            val refreshToken = jwtService.generateRefreshToken(userId)

            storeRefreshToken(userId, refreshToken)

            AuthenticatedUser(
                user = user.toUser(),
                accessToken = accessToken,
                refreshToken = refreshToken,
            )
        } ?: throw UserNotFoundException()
    }

    @Transactional
    fun refresh(refreshToken: String): AuthenticatedUser {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw InvalidTokenException(
                message = "Invalid refresh token"
            )
        }
        val userId = jwtService.getUserIdFromToken(refreshToken)
        val user = userRepository.findByIdOrNull(userId)
            ?: throw UserNotFoundException()

        val hashed = hashToken(refreshToken)

        return user.id?.let { userId ->
            refreshTokenRepository.findByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashed
            ) ?: throw InvalidTokenException("Invalid refresh token")

            refreshTokenRepository.deleteByUserIdAndHashedToken(
                userId = userId,
                hashedToken = hashed,
            )

            val newAccessToken = jwtService.generateAccessToken(userId)
            val newRefreshToken = jwtService.generateRefreshToken(userId)

            storeRefreshToken(userId, newRefreshToken)

            AuthenticatedUser(
                user = user.toUser(),
                accessToken = newAccessToken,
                refreshToken = newRefreshToken,
            )
        } ?: throw UserNotFoundException()
    }

    @Transactional
    fun logout(refreshToken: String) {
        val userId = jwtService.getUserIdFromToken(refreshToken)
        val hashed = hashToken(refreshToken)

        refreshTokenRepository.deleteByUserIdAndHashedToken(userId, hashed)
    }

    private fun storeRefreshToken(userId: UserId, token: String) {
        val hashed = hashToken(token)
        val expiryMs = jwtService.refreshTokenValidityMs
        val expiresAt = Instant.now().plusMillis(expiryMs)

        refreshTokenRepository.save(
            RefreshTokenEntity(
                userId = userId,
                expiresAt = expiresAt,
                hashedToken = hashed,
            )
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.encodeToByteArray())

        return Base64.getEncoder().encodeToString(hashBytes)
    }

    companion object {
        // pre-computed Bcrypt hash of "dodotalk-password-to-prevent-timing-attack-from-hacker"
        // Uses cost factor 10 (2^10 rounds) to match production password encoder settings
        // This ensures constant-time authentication regardless of whether user exists
        private const val DUMMY_PASSWORD_HASH = "\$2a\$10\$VGAtbsYqzXGxxQ3N6NW4pOrulLW8TqTgcQkyylrjZVN4KWM.V7ncG"
    }
}