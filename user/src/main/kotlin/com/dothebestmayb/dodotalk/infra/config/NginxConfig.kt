package com.dothebestmayb.dodotalk.infra.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * yml을 설정할 때, 아래와 같이 property를 공통과 각각에 대해 다르게 선언할 수 없음
 *
 * ```yml
 * // 공통
 * a:
 *  b: something
 *
 * // dev
 * a:
 *  c: false
 *
 * // prod
 * a:
 *  c: true
 *  ```
 * 이 클래스는 이것을 가능하게 하기 위한 설정임
 */
@Configuration
@ConfigurationProperties(prefix = "nginx")
data class NginxConfig(
    var trustedIps: List<String> = emptyList(),
    var requireProxy: Boolean = true,
)