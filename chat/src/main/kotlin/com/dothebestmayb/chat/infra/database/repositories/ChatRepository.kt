package com.dothebestmayb.chat.infra.database.repositories

import com.dothebestmayb.chat.infra.database.entities.ChatEntity
import com.dothebestmayb.dodotalk.domain.type.ChatId
import com.dothebestmayb.dodotalk.domain.type.UserId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface ChatRepository: JpaRepository<ChatEntity, ChatId> {
    /**
     * FETCH : 연관된 엔티티나 컬렉션을 한 번의 SQL 쿼리로 즉시 함께 조회(Eager Loading)
     *
     * 만약 FETCH를 쓰지 않고 ChatEntity 100건을 조회한 뒤, 각 엔티티의 creator 이름을 출력하려고 하면, 처음 1번의 쿼리 외에 각 creator를 찾기 위한 100번의 추가 쿼리가 발생할 수 있습니다(N+1 문제). FETCH JOIN을 사용하면 처음부터 조인을 통해 모든 데이터를 가져오므로 단 1번의 쿼리로 모든 데이터를 처리할 수 있어 성능이 매우 향상됩니다.
     */
    @Query("""
        SELECT c
        FROM ChatEntity c
        LEFT JOIN FETCH c.participants
        LEFT JOIN FETCH c.creator
        WHERE c.id = :id
        AND EXISTS (
            SELECT 1
            FROM c.participants p
            WHERE p.userId = :userId
        )
    """)
    /**
     * 채팅방을 얻기 위해 chatId로도 충분하지만, 보안을 위해 User가 채팅방에 속하는지 확인함
     */
    fun findChatById(id: ChatId, userId: UserId): ChatEntity?

    @Query("""
        SELECT c
        FROM ChatEntity c
        LEFT JOIN FETCH c.participants
        LEFT JOIN FETCH c.creator
        WHERE EXISTS (
            SELECT 1
            FROM c.participants p
            WHERE p.userId = :userId
        )
    """)
    fun findAllByUserId(userId: UserId): List<ChatEntity>
}