package com.dothebestmayb.dodotalk.api.dto.ws

enum class IncomingWebSocketMessageType {
    NEW_MESSAGE
}

enum class OutgoingWebSocketMessageType {
    NEW_MESSAGE,
    MESSAGE_DELETED,
    PROFILE_PICTURE_UPDATED,
    CHAT_PARTICIPANTS_CHANGED,
    ERROR
}

/**
 * @param type payload(json)는 클라이언트가 요청하는 데이터에 따라 구조가 다를 수 있다. 서버는 type을 보고 payload를 가변적으로 역직렬화한다.
 */
data class IncomingWebSocketMessage(
    val type: IncomingWebSocketMessageType,
    val payload: String,
)

data class OutgoingWebSocketMessage(
    val type: OutgoingWebSocketMessageType,
    val payload: String,
)