package com.dothebestmayb.dodotalk.api.exception_handling

import com.dothebestmayb.dodotalk.domain.exception.ChatNotFoundException
import com.dothebestmayb.dodotalk.domain.exception.ChatParticipantNotFoundException
import com.dothebestmayb.dodotalk.domain.exception.InvalidChatSizeException
import com.dothebestmayb.dodotalk.domain.exception.MessageNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ChatExceptionHandler {

    @ExceptionHandler(
        ChatNotFoundException::class,
        MessageNotFoundException::class,
        ChatParticipantNotFoundException::class,
    )
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun onNotFound(e: Exception) = mapOf(
        "code" to "NOT_FOUND",
        "message" to e.message
    )

    @ExceptionHandler(InvalidChatSizeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun onInvalidChatSize(e: InvalidChatSizeException) = mapOf(
        "code" to "INVALID_CHAT_SIZE",
        "message" to e.message
    )
}