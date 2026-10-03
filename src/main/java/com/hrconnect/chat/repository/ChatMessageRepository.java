package com.hrconnect.chat.repository;

import com.hrconnect.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /** Trang dau tien: cac tin moi nhat, xep tu moi den cu. */
    @Query(value = """
            SELECT * FROM chat_messages
            WHERE conversation_id = :conversationId
            ORDER BY created_at DESC, id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ChatMessage> findLatest(@Param("conversationId") UUID conversationId, @Param("limit") int limit);

    /**
     * Trang tiep theo khi cuon len: cac tin cu hon tin {@code beforeId}.
     * So sanh theo cap (created_at, id) de khong bo sot tin co cung thoi diem.
     */
    @Query(value = """
            SELECT * FROM chat_messages
            WHERE conversation_id = :conversationId
              AND (created_at, id) < (SELECT c.created_at, c.id FROM chat_messages c WHERE c.id = :beforeId)
            ORDER BY created_at DESC, id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ChatMessage> findBefore(@Param("conversationId") UUID conversationId,
                                 @Param("beforeId") UUID beforeId,
                                 @Param("limit") int limit);

    /** Tim tin theo tu khoa trong mot hoi thoai (khong phan biet hoa thuong, bo qua tin da thu hoi). */
    @Query(value = """
            SELECT * FROM chat_messages
            WHERE conversation_id = :conversationId
              AND recalled_at IS NULL
              AND content ILIKE CONCAT('%', :pattern, '%')
            ORDER BY created_at DESC, id DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ChatMessage> search(@Param("conversationId") UUID conversationId,
                             @Param("pattern") String escapedPattern,
                             @Param("limit") int limit);
}
