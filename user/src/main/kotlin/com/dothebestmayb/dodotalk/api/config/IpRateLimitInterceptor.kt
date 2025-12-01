package com.dothebestmayb.dodotalk.api.config

import com.dothebestmayb.dodotalk.domain.exception.RateLimitException
import com.dothebestmayb.dodotalk.infra.rate_limiting.IpRateLimiter
import com.dothebestmayb.dodotalk.infra.rate_limiting.IpResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration

@Component
class IpRateLimitInterceptor(
    private val ipRateLimiter: IpRateLimiter,
    private val ipResolver: IpResolver,
    @param:Value("\${dodotalk.rate-limit.ip.apply-limit}")
    private val applyLimit: Boolean,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod && applyLimit) {
            val annotation = handler.getMethodAnnotation(IpRateLimit::class.java)
            if (annotation != null) {
                val clientIp = ipResolver.getClientIp(request)

                return try {
                    ipRateLimiter.withIpRateLimit(
                        ipAddress = clientIp,
                        resetsIn = Duration.of(
                            annotation.duration,
                            annotation.unit.toChronoUnit(),
                        ),
                        maxRequestsPerIp = annotation.requests,
                        // withIpRateLimit 함수가 성공적으로 수행되었을 때, action 람다 블록이 리턴되므로 true로 설정
                        action = { true },
                    )
                } catch (e: RateLimitException) {
                    response.sendError(HttpStatus.TOO_MANY_REQUESTS.value())
                    false
                }
            }
        }

        return true
    }
}