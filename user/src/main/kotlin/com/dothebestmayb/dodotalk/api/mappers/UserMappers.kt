package com.dothebestmayb.dodotalk.api.mappers

import com.dothebestmayb.dodotalk.api.dto.AuthenticatedUserDto
import com.dothebestmayb.dodotalk.api.dto.UserDto
import com.dothebestmayb.dodotalk.domain.model.AuthenticatedUser
import com.dothebestmayb.dodotalk.domain.model.User

fun AuthenticatedUser.toAuthenticatedUserDto(): AuthenticatedUserDto {
    return AuthenticatedUserDto(
        user = user.toUserDto(),
        accessToken = accessToken,
        refreshToken = refreshToken,
    )
}

fun User.toUserDto(): UserDto {
    return UserDto(
        id = id,
        email = email,
        username = username,
        hasVerifiedEmail = hasEmailVerified,
    )
}