package com.dothebestmayb.dodotalk.security

import com.dothebestmayb.dodotalk.api.config.JwtAuthFilter
import com.dothebestmayb.dodotalk.service.JwtService
import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig {

    @Bean
    fun filterChain(httpSecurity: HttpSecurity, jwtService: JwtService): SecurityFilterChain {

        val jwtAuthFilter = JwtAuthFilter(jwtService)

        return httpSecurity
            .csrf { it.disable() } // jwt token으로 대체
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/.well-known/**")
                    .permitAll()
                    // change-password api를 요청할 때, JWT token이 첨부되었는지 SpringBoot가 확인하도록 설정
                    .requestMatchers("/api/auth/change-password")
                    .authenticated()
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    .dispatcherTypeMatchers(
                        DispatcherType.ERROR,
                        DispatcherType.FORWARD
                    )
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            // authorizeHttpRequests가 permit 되지 않았을 때 어떻게 처리할지 설정하는 코드
            .exceptionHandling { configurer ->
                configurer
                    .authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            }
            .build()
    }
}