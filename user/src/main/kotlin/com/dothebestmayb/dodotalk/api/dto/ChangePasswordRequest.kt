package com.dothebestmayb.dodotalk.api.dto

import com.dothebestmayb.dodotalk.api.util.Password
import jakarta.validation.constraints.NotBlank

data class ChangePasswordRequest(
    @field:NotBlank
    val oldPassword: String,
    @field:Password
    val newPassword: String,
)
