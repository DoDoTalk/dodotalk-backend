package com.dothebestmayb.dodotalk

import com.dothebestmayb.dodotalk.infra.database.entities.UserEntity
import com.dothebestmayb.dodotalk.infra.database.repositories.UserRepository
import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component

@SpringBootApplication
@EnableScheduling
class DodotalkBackendApplication

fun main(args: Array<String>) {
    runApplication<DodotalkBackendApplication>(*args)
}

/**
 * JPA로 설정한 테이블이 생성되려면 최초로 한 번 데이터를 저장해야 한다.
 * 아래는 데이터 생성을 확인하기 위한 테스트 코드이다.
 */
//@Component
//class Demo(
//    private val repository: UserRepository
//) {
//    @PostConstruct
//    fun init() {
//        repository.save(
//            UserEntity(
//                email = "test@test.com",
//                username = "test",
//                hashedPassword = "123",
//            )
//        )
//    }
//}
