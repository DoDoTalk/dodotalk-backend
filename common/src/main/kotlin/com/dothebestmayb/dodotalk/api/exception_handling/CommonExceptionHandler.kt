package com.dothebestmayb.dodotalk.api.exception_handling

import com.dothebestmayb.dodotalk.domain.exception.ForbiddenException
import com.dothebestmayb.dodotalk.domain.exception.UnauthorizedException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class CommonExceptionHandler {

    @ExceptionHandler(ForbiddenException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun onForbidden(e: ForbiddenException) = mapOf(
        "code" to "FORBIDDEN",
        "message" to e.message
    )

    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun onUnauthorized(e: UnauthorizedException) = mapOf(
        "code" to "UNAUTHORIZED",
        "message" to e.message
    )
}