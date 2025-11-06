package com.dothebestmayb.dodotalk.service.auth

import com.dothebestmayb.dodotalk.domain.exception.UserAlreadyExistsException
import com.dothebestmayb.dodotalk.domain.model.User
import com.dothebestmayb.dodotalk.infra.database.entities.UserEntity
import com.dothebestmayb.dodotalk.infra.database.mapper.toUser
import com.dothebestmayb.dodotalk.infra.database.repositories.UserRepository
import com.dothebestmayb.dodotalk.infra.security.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
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
}