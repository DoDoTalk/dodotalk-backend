package com.dothebestmayb.dodotalk.infra.rate_limiting

import com.dothebestmayb.dodotalk.infra.config.NginxConfig
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.security.web.util.matcher.IpAddressMatcher
import org.springframework.stereotype.Component
import java.net.Inet4Address
import java.net.Inet6Address

@Component
class IpResolver(
    private val nginxConfig: NginxConfig,
) {

    private val trustedMatchers: List<IpAddressMatcher> = nginxConfig
        .trustedIps
        .filter { it.isNotBlank() }
        .map { proxy ->
            // cidr : Classless Inter Domain Routing
            // cidr 표기법 : IP 주소 + 슬래시(/) + 숫자(prefix 길이)
            val cidr = when {
                proxy.contains("/") -> proxy // already range specified
                proxy.count { it == ':' } >= 2 -> "$proxy/128" // IPv6
                else -> "$proxy/32" // IPv4
            }
            IpAddressMatcher(cidr)
        }

    private val logger = LoggerFactory.getLogger(IpResolver::class.java)

    private val ipv4Regex = Regex("\\d+\\.\\d+\\.\\d+\\.\\d+")

    /**
     * nginx와 같이 reverse proxy를 사용하는 경우, `request.remoteAddr`는 client ip가 아닌 nginx ip임
     * 또한 RESTful server의 경우, 다른 백엔드 서버가 request를 전달할 수도 있음
     * 즉, request의 ip는 last hop ip를 가리킨다.
     */
    fun getClientIp(request: HttpServletRequest): String {
        val remoteAddr = request.remoteAddr

        if (!isFromTrustedProxy(remoteAddr)) {
            if (nginxConfig.requireProxy) {
                logger.warn("Direct connection attempt from $remoteAddr")
                throw SecurityException("No valid client IP in proxy headers")
            }

            return remoteAddr
        }

        val clientIp = extractFromXRealIp(request, remoteAddr)

        if (clientIp == null) {
            logger.warn("No valid client IP in proxy headers")
            if (nginxConfig.requireProxy) {
                throw SecurityException("No valid client IP in proxy headers")
            }
        }

        return clientIp ?: remoteAddr
    }

    private fun extractFromXRealIp(
        request: HttpServletRequest,
        proxyIp: String,
    ): String? {
        return request.getHeader("X-Real-IP")?.let { header ->
            validateAndNormalizeIp(header, "X-Real-IP", proxyIp)
        }
    }

    /**
     * "::1"와 같은 표현을 "0:0:0:..."와 같이 full representation으로 변환함
     */
    private fun validateAndNormalizeIp(ip: String, headerName: String, proxyIp: String): String? {
        val trimmedIp = ip.trim()

        if (trimmedIp.isBlank() || INVALID_IPS.contains(trimmedIp)) {
            logger.debug("Invalid IP in $headerName: $ip from proxy $proxyIp")
            return null
        }

        return try {
            val inetAddr = when {
                trimmedIp.contains(":") -> Inet6Address.getByName(trimmedIp)
                trimmedIp.matches(ipv4Regex) -> Inet4Address.getByName(trimmedIp)
                else -> {
                    logger.warn("Invalid IP format in $headerName: $trimmedIp from proxy $proxyIp")
                    return null
                }
            }

            if (isPrivateIp(inetAddr.hostAddress)) {
                logger.debug("Private IP in $headerName: $trimmedIp from proxy $proxyIp")
            }

            inetAddr.hostAddress
        } catch (e: Exception) {
            logger.warn("Invalid IP format in $headerName: $trimmedIp from proxy $proxyIp", e)
            null
        }
    }

    private fun isPrivateIp(ip: String): Boolean {
        return PRIVATE_IP_RANGES.any { it.matches(ip) }
    }

    private fun isFromTrustedProxy(ip: String): Boolean {
        return trustedMatchers.any { matcher ->
            matcher.matches(ip)
        }

    }

    companion object {
        private val PRIVATE_IP_RANGES = listOf(
            "10.0.0.0/8",      // 10.x.x.x 사설 IPv4 대역 (회사 내부망, VPC, 컨테이너 네트워크 등)
            "172.16.0.0/12",   // 172.16.0.0~172.31.255.255 사설 IPv4 대역 (Docker/k8s 브리지 등 내부 서비스)
            "192.168.0.0/16",  // 192.168.x.x 사설 IPv4 대역 (가정/사무실 공유기 LAN)
            "127.0.0.0/8",     // 서버 자신을 가리키는 IPv4 루프백 대역 (localhost, 같은 머신의 nginx 등)
            "::1/128",         // 서버 자신을 가리키는 IPv6 루프백 주소 (IPv6 localhost)
            "fc00::/7",        // 조직 내부에서만 사용하는 IPv6 사설(ULA) 주소 대역
            "fe80::/10"        // 같은 링크/서브넷에서만 통신하는 IPv6 링크 로컬 주소 대역
        ).map { IpAddressMatcher(it) }

        private val INVALID_IPS = listOf(
            "unknown", // 프록시나 애플리케이션이 클라이언트 ip를 알 수 없을 때
            "unavailable", // IP 정보를 사용할 수 없는 경우
            // 주소로 사용하는 경우 : 서버가 실행되는 기기의 모든 IPv4 주소
            // 클라이언트로부터의 주소인 경우 : 유효한 출발지 IP가 없거나, 아직 정해지지 않은 상태
            "0.0.0.0",
            "::" // IPv6에서의 미지정 주소, IPv4의 0.0.0.0과 동일한 역할
        )
    }
}