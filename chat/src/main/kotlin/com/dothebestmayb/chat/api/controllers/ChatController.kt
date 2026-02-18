package com.dothebestmayb.chat.api.controllers

import com.dothebestmayb.chat.api.dto.ChatDto
import com.dothebestmayb.chat.api.dto.CreateChatRequest
import com.dothebestmayb.chat.api.mappers.toChatDto
import com.dothebestmayb.chat.service.ChatService
import com.dothebestmayb.dodotalk.api.util.requestUserId
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService,
) {

    @PostMapping()
    fun createChat(
        @Valid @RequestBody body: CreateChatRequest
    ): ChatDto {
        return chatService.createChat(
            creatorId = requestUserId,
            otherUserIds = body.otherUserIds.toSet(),
        ).toChatDto()
    }
}