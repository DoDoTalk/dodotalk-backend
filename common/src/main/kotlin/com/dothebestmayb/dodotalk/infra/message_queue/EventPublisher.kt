package com.dothebestmayb.dodotalk.infra.message_queue

import com.dothebestmayb.dodotalk.domain.events.DoDoTalkEvent
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class EventPublisher(
    private val rabbitTemplate: RabbitTemplate
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun <T: DoDoTalkEvent> publish(event: T) {
        try {
            rabbitTemplate.convertAndSend(
                event.exchange,
                event.eventKey,
                event,
            )
            logger.info("Successfully published event ${event.eventKey}")
        } catch (e: Exception) {
            logger.error("Failed to publish ${event.eventKey} event", e)
        }
    }
}