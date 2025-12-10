package com.dothebestmayb.dodotalk.domain.events

import java.time.Instant

/**
 * @param eventKey 어떤 이벤트가 발생했는지를 나타내는 식별자
 * @param exchange event를 전달받아 이것을 처리할 queue에 전달해주는 역할을 수행하는 대상. cf) reroute
 */
interface DoDoTalkEvent {
    val eventId: String
    val eventKey: String
    val occurredAt: Instant
    val exchange: String
}