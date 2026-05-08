package com.dothebestmayb.dodotalk.api.websocket

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "dodotalk.web-socket")
class WebSocketProperties(
    var allowedOrigins: List<String> = emptyList(),
)