package com.dothebestmayb.dodotalk.infra.messaging

import com.dothebestmayb.dodotalk.domain.models.ChatParticipant
import com.dothebestmayb.dodotalk.service.ChatParticipantService
import com.dothebestmayb.dodotalk.domain.events.user.UserEvent
import com.dothebestmayb.dodotalk.infra.message_queue.MessageQueues
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component


@Component
class ChatUserEventListener(
    private val chatParticipantService: ChatParticipantService
) {

    @RabbitListener(
        queues = [MessageQueues.CHAT_USER_EVENTS]
    )
    fun handleUserEvent(event: UserEvent) {
        when (event) {
            is UserEvent.Verified -> {
                chatParticipantService.createChatParticipant(
                    chatParticipant = ChatParticipant(
                        userId = event.userId,
                        username = event.username,
                        email = event.email,
                        profilePictureUrl = null,
                    )
                )
            }
            else -> Unit
        }
    }
}