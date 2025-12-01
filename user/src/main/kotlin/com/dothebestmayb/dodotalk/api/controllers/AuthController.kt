package com.dothebestmayb.dodotalk.api.controllers

import com.dothebestmayb.dodotalk.api.config.IpRateLimit
import com.dothebestmayb.dodotalk.api.dto.AuthenticatedUserDto
import com.dothebestmayb.dodotalk.api.dto.ChangePasswordRequest
import com.dothebestmayb.dodotalk.api.dto.EmailRequest
import com.dothebestmayb.dodotalk.api.dto.LoginRequest
import com.dothebestmayb.dodotalk.api.dto.RefreshRequest
import com.dothebestmayb.dodotalk.api.dto.RegisterRequest
import com.dothebestmayb.dodotalk.api.dto.ResetPasswordRequest
import com.dothebestmayb.dodotalk.api.dto.UserDto
import com.dothebestmayb.dodotalk.api.mappers.toAuthenticatedUserDto
import com.dothebestmayb.dodotalk.api.mappers.toUserDto
import com.dothebestmayb.dodotalk.api.util.requestUserId
import com.dothebestmayb.dodotalk.infra.rate_limiting.EmailRateLimiter
import com.dothebestmayb.dodotalk.service.AuthService
import com.dothebestmayb.dodotalk.service.EmailVerificationService
import com.dothebestmayb.dodotalk.service.PasswordResetService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
    private val emailVerificationService: EmailVerificationService,
    private val passwordResetService: PasswordResetService,
    private val emailRateLimiter: EmailRateLimiter,
) {

    @PostMapping("/register")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS,
    )
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
    @IpRateLimit(
        requests = 20,
        duration = 1L,
        unit = TimeUnit.HOURS,
    )
    fun login(
        @RequestBody body: LoginRequest
    ): AuthenticatedUserDto {
        return authService.login(
            email = body.email,
            password = body.password
        ).toAuthenticatedUserDto()
    }

    @PostMapping("/refresh")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS,
    )
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

    @PostMapping("/resend-verification")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS,
    )
    fun resendVerification(
        @Valid @RequestBody body: EmailRequest,
    ) {
        emailRateLimiter.withRateLimit(
            email = body.email,
        ) {
            emailVerificationService.resendVerificationEmail(body.email)
        }
    }

    @GetMapping("/verify")
    fun verifyEmail(
        @RequestParam token: String
    ) {
        emailVerificationService.verifyEmail(token)
    }

    @PostMapping("/forgot-password")
    @IpRateLimit(
        requests = 10,
        duration = 1L,
        unit = TimeUnit.HOURS,
    )
    fun forgotPassword(
        @Valid @RequestBody body: EmailRequest
    ) {
        passwordResetService.requestPasswordReset(body.email)
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @Valid @RequestBody body: ResetPasswordRequest
    ) {
        passwordResetService.resetPassword(
            token = body.token,
            newPassword = body.newPassword,
        )
    }

    @PostMapping("/change-password")
    fun changePassword(
        @Valid @RequestBody body: ChangePasswordRequest
    ) {
        passwordResetService.changePassword(
            userId = requestUserId,
            oldPassword = body.oldPassword,
            newPassword = body.newPassword,
        )
    }
}