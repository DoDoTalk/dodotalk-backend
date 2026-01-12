package com.dothebestmayb.chat.infra.database.repositories

import com.dothebestmayb.chat.infra.database.entities.ChatMessageEntity
import com.dothebestmayb.dodotalk.domain.type.ChatId
import com.dothebestmayb.dodotalk.domain.type.ChatMessageId
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface ChatMessageRepository: JpaRepository<ChatMessageEntity, ChatMessageId> {

    /**
     * [No-Offset 기반 무한 스크롤 조회]
     * * 1. 커서 기반 페이징 (Cursor-based Pagination):
     * - 일반적인 Page 방식(Offset)은 새로운 메시지가 실시간으로 추가될 때 데이터 중복이나 누락이 발생함.
     * - '기준점(createdAt)'을 사용해 특정 시점 이전의 데이터만 가져옴으로써 데이터 무결성을 보장함.
     * * 2. Slice를 사용하는 이유:
     * - Page는 전체 데이터 개수(Total Count)를 구하기 위해 추가적인 COUNT 쿼리를 실행함 (성능 부하 발생).
     * - 채팅처럼 전체 페이지 수가 중요하지 않고 "다음 데이터 존재 여부"만 필요한 경우,
     * - - 전체 개수를 세지 않음. 요청한 사이즈가 20개라면 내부적으로 21개를 조회함. 21번째 데이터가 있으면 hasNext = true를 반환함.
     * Slice를 사용하여 COUNT 쿼리 없이 성능을 최적화함 (내부적으로 limit + 1 조회).
     * * 3. 정렬 및 필터링:
     * - 최신 메시지부터 보여주기 위해 DESC(내림차순) 정렬 사용.
     * - :before 파라미터로 마지막으로 읽은 메시지 시점을 전달받아 그 이전 데이터만 필터링.
     */
    @Query("""
        SELECT m
        FROM ChatMessageEntity m
        WHERE m.chatId = :chatId
        AND m.createdAt < :before
        ORDER BY m.createdAt DESC 
    """)
    fun findByChatIdBefore(
        chatId: ChatId,
        before: Instant,
        pageable: Pageable,
    ): Slice<ChatMessageEntity>

    @Query("""
        SELECT m
        FROM ChatMessageEntity m
        LEFT JOIN FETCH m.sender
        WHERE m.chatId IN :chatIds
        AND (m.createdAt, m.id) = (
            SELECT m2.createdAt, m2.id
            FROM ChatMessageEntity m2
            WHERE m2.chatId = m.chatId
            ORDER BY m2.createdAt DESC
            LIMIT 1
        )
    """)
    fun findLatestMessageByChatIds(
        chatIds: Set<ChatId>,
    ): List<ChatMessageEntity>
}