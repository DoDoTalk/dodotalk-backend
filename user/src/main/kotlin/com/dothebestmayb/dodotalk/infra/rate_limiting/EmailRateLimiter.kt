package com.dothebestmayb.dodotalk.infra.rate_limiting

import com.dothebestmayb.dodotalk.domain.exception.RateLimitException
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component

@Component
class EmailRateLimiter(
    private val redisTemplate: StringRedisTemplate
) {
    /**
     * classpath는 JVM이 .class 파일이나 리소스를 검색하는 경로 목록이다.
     * 빌드로 jar 파일을 생성할 때, resources 패키지에 있는 파일들은 classpath에 들어간다.
     */
    @Value("classpath:email_rate_limit.lua")
    lateinit var rateLimitResource: Resource

    private val rateLimitScript by lazy {
        val script = rateLimitResource.inputStream.use {
            it.readBytes().decodeToString()
        }
        @Suppress("UNCHECKED_CAST")
        DefaultRedisScript(script, List::class.java as Class<List<Long>>)
    }

    fun withRateLimit(
        email: String,
        action: () -> Unit,
    ) {
        val normalizedEmail = email.lowercase().trim()

        val rateLimitKey = "$EMAIL_RATE_LIMIT_PREFIX:$normalizedEmail"
        val attemptCountKey = "$EMAIL_ATTEMPT_COUNT_PREFIX:$normalizedEmail"

        // redis execute block에서 read and write를 동시에 atomic하게 처리할 수 없다.
        // lua script를 이용해서 이 문제를 해결할 수 있다.
        val result = redisTemplate.execute(
            rateLimitScript,
            listOf(rateLimitKey, attemptCountKey)
        )

        val attemptCount = result[0]
        val ttl = result[1]

        if (attemptCount == -1L) {
            throw RateLimitException(resetsInSeconds = ttl)
        }

        action()
    }



    companion object {
        private const val EMAIL_RATE_LIMIT_PREFIX = "rate_limit:email"
        private const val EMAIL_ATTEMPT_COUNT_PREFIX = "email_attempt_count"
    }
}