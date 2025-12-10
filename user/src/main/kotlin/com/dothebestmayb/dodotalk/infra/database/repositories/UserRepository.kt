package com.dothebestmayb.dodotalk.infra.database.repositories

import com.dothebestmayb.dodotalk.domain.type.UserId
import com.dothebestmayb.dodotalk.infra.database.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<UserEntity, UserId> {
    fun findByEmail(email: String): UserEntity?
    fun findByEmailOrUsername(email: String, username: String): UserEntity?
}