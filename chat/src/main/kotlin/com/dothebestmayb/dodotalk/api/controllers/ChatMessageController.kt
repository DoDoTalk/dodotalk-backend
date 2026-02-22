package com.dothebestmayb.dodotalk.api.controllers

import com.dothebestmayb.dodotalk.api.util.requestUserId
import com.dothebestmayb.dodotalk.domain.type.ChatMessageId
import com.dothebestmayb.dodotalk.service.ChatMessageService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/messages")
class ChatMessageController(private val chatMessageService: ChatMessageService) {

    @DeleteMapping("/{messageId}")
    fun deleteMessage(
        @PathVariable messageId: ChatMessageId
    ) {
        chatMessageService.deleteMessage(messageId, requestUserId)
    }
}