package com.hrconnect.chat.repository;

import com.hrconnect.chat.domain.ChatParticipant;
import com.hrconnect.chat.domain.ChatParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, ChatParticipantId> {

    /** Tat ca hoi thoai ma user tham gia. */
    List<ChatParticipant> findByIdUserId(UUID userId);

    @Modifying
    @Query(value = """
            INSERT INTO chat_participants (conversation_id, user_id)
            VALUES (:conversationId, :userId)
            ON CONFLICT (conversation_id, user_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);

    /**
     * So tin chua doc cua user trong tung hoi thoai: tin do nguoi kia gui,
     * moi hon tin ma user da doc toi (chua doc tin nao thi dem het).
     * Moi phan tu: [0] = conversation_id (UUID), [1] = so luong (Number).
     */
    @Query(value = """
            SELECT m.conversation_id, COUNT(*)
            FROM chat_messages m
            JOIN chat_participants p
              ON p.conversation_id = m.conversation_id AND p.user_id = :userId
            LEFT JOIN chat_messages lr
              ON lr.id = p.last_read_message_id
            WHERE m.sender_id <> :userId
              AND (lr.id IS NULL OR (m.created_at, m.id) > (lr.created_at, lr.id))
            GROUP BY m.conversation_id
            """, nativeQuery = true)
    List<Object[]> countUnreadByConversation(@Param("userId") UUID userId);
}
