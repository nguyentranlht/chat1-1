package com.hrconnect.chat.repository;

import com.hrconnect.chat.domain.ChatPushToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ChatPushTokenRepository extends JpaRepository<ChatPushToken, String> {

    List<ChatPushToken> findByUserId(UUID userId);

    /** Them token, hoac chuyen token sang user moi neu trinh duyet nay doi nguoi dang nhap. */
    @Modifying
    @Query(value = """
            INSERT INTO chat_push_tokens (token, user_id, platform)
            VALUES (:token, :userId, :platform)
            ON CONFLICT (token) DO UPDATE
               SET user_id = EXCLUDED.user_id, platform = EXCLUDED.platform, updated_at = now()
            """, nativeQuery = true)
    int upsert(@Param("token") String token, @Param("userId") UUID userId, @Param("platform") String platform);

    /** Chi xoa duoc token cua chinh minh. */
    @Modifying
    @Query("DELETE FROM ChatPushToken t WHERE t.token = :token AND t.userId = :userId")
    int deleteOwned(@Param("token") String token, @Param("userId") UUID userId);

    /** Xoa cac token ma Firebase bao da het han / khong hop le. */
    @Modifying
    @Query("DELETE FROM ChatPushToken t WHERE t.token IN :tokens")
    int deleteAllByTokens(@Param("tokens") Collection<String> tokens);
}
