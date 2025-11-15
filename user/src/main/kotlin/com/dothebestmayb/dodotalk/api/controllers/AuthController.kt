package com.dothebestmayb.dodotalk.api.controllers

import com.dothebestmayb.dodotalk.api.dto.AuthenticatedUserDto
import com.dothebestmayb.dodotalk.api.dto.LoginRequest
import com.dothebestmayb.dodotalk.api.dto.RefreshRequest
import com.dothebestmayb.dodotalk.api.dto.RegisterRequest
import com.dothebestmayb.dodotalk.api.dto.UserDto
import com.dothebestmayb.dodotalk.api.mappers.toAuthenticatedUserDto
import com.dothebestmayb.dodotalk.api.mappers.toUserDto
import com.dothebestmayb.dodotalk.service.auth.AuthService
import com.dothebestmayb.dodotalk.service.auth.EmailVerificationService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService,
                     private val emailVerificationService: EmailVerificationService
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody body: RegisterRequest
    ): UserDto {
        return authService.register(
            email = body.email,
            username = body.username,
            password = body.password
        ).toUserDto()
    }

    @PostMapping("/login")
    fun login(
        @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return authService.login(
            email = body.email,
            password = body.password
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    fun refresh(
        @RequestBody body: RefreshRequest
    ): AuthenticatedUserDto {
        return authService
            .refresh(body.refreshToken)
            .toAuthenticatedUserDto()
    }

    @PostMapping("/logout")
    fun logout(
        @RequestBody body: RefreshRequest
    ) {
        authService.logout(body.refreshToken)
    }

    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam token: String
    ) {
        emailVerificationService.verifyEmail(token)
    }
}