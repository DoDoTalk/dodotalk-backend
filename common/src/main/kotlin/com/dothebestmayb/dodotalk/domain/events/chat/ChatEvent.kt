package com.dothebestmayb.dodotalk.domain.events.chat

import com.dothebestmayb.dodotalk.domain.events.DoDoTalkEvent
import com.dothebestmayb.dodotalk.domain.type.ChatId
import com.dothebestmayb.dodotalk.domain.type.UserId
import java.time.Instant
import java.util.UUID

sealed class ChatEvent(
    override val eventId: String = UUID.randomUUID().toString(),
    override val exchange: String = ChatEventConstants.CHAT_EXCHANGE,
    override val occurredAt: Instant = Instant.now(),
) : DoDoTalkEvent {

    /**
     * @param recipientIds notificationService는 해당 메시지를 누구에게 전달해야 하는지 모르기 때문에 명시
     */
    data class NewMessage(
        val senderId: UserId,
        val senderUsername: String,
        val recipientIds: Set<UserId>,
        val chatId: ChatId,
        val message: String,
        override val eventKey: String = ChatEventConstants.CHAT_NEW_MESSAGE
    ): ChatEvent()
}