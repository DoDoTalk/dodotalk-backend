package com.dothebestmayb.dodotalk.service.auth

import com.dothebestmayb.dodotalk.domain.exception.InvalidCredentialsException
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
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository,
) {

    fun register(email: String, username: String, password: String): User {
        val user = userRepository.findByEmailOrUsername(
            email = email.trim(),
            username = username.trim(),
        )
        if (user != null) {
            throw UserAlreadyExistsException()
        }
        val savedUser = userRepository.save(
            UserEntity(
                email = email.trim(),
                username = username.trim(),
                hashedPassword = passwordEncoder.encode(password),
            )
        ).toUser()

        return savedUser
    }

    fun login(
        email: String,
        password: String,
    ): AuthenticatedUser {
        val user = userRepository.findByEmail(email.trim())
            ?: throw InvalidCredentialsException()

        if (!passwordEncoder.matches(password, user.hashedPassword)) {
            throw InvalidCredentialsException()
        }

        // TODO : check for verified email

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
}