package com.hrconnect.chat.repository;

import com.hrconnect.chat.domain.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, UUID> {

    Optional<ChatConversation> findByUserAIdAndUserBId(UUID userAId, UUID userBId);

    /**
     * Tao hoi thoai neu cap nay chua co. Neu hai request chay cung luc,
     * chi mot dong duoc tao, request con lai "do nothing" va khong loi.
     */
    @Modifying
    @Query(value = """
            INSERT INTO chat_conversations (id, user_a_id, user_b_id)
            VALUES (:id, :userA, :userB)
            ON CONFLICT (user_a_id, user_b_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id, @Param("userA") UUID userA, @Param("userB") UUID userB);
}
