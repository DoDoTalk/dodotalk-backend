package com.dothebestmayb.dodotalk.api.dto

import com.dothebestmayb.dodotalk.api.util.Password
import jakarta.validation.constraints.NotBlank

data class ResetPasswordRequest(
    @field:NotBlank
    val token: String,
    @field:Password
    val newPassword: String,
)
