package com.dothebestmayb.chat.infra.database.entities

import com.dothebestmayb.dodotalk.domain.type.ChatId
import com.dothebestmayb.dodotalk.domain.type.ChatMessageId
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.Instant

@Entity
@Table(
    name = "chat_message",
    schema = "chat_service",
    indexes = [
        // 특정 채팅방에 속한 메시지를 최신 순으로 빠르게 얻기 위해 index 설정
        Index(
            name = "idx_chat_message_chat_id_created_at",
            columnList = "chat_id,created_at DESC"
        )
    ]
)
class ChatMessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: ChatMessageId? = null,
    @Column(nullable = false)
    var content: String,
    // chat 파라미터를 통해 chatId를 얻을 수 있지만, chatId 1개를 얻기 위해 한 번의 query를 수행해야 한다.
    // query 연산을 줄이기 위해 자주 사용되는 chatId를 파라미터로 저장한다.
    @JoinColumn(
        name = "chat_id",
        nullable = false,
        updatable = false,
    )
    var chatId: ChatId,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "chat_id",
        nullable = false,
        insertable = false,
        updatable = false,
    )
    // non-null로 선언하면 Hibernate가 auto populate 하기 위한 기본 값을 요구한다.
    var chat: ChatEntity? = null,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "sender_id",
        nullable = false,
        insertable = false,
        updatable = false,
    )
    var sender: ChatParticipantEntity? = null,
    @CreationTimestamp
    var createdAt: Instant = Instant.now(),
)