package com.hrconnect.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** FCM token cua mot trinh duyet / thiet bi. Tao va cap nhat qua {@code ChatPushTokenRepository.upsert}. */
@Entity
@Table(name = "chat_push_tokens")
public class ChatPushToken {

    @Id
    private String token;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false)
    private PushPlatform platform;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ChatPushToken() {
        // JPA
    }

    /** Dung trong test. */
    public ChatPushToken(String token, UUID userId, PushPlatform platform) {
        this.token = token;
        this.userId = userId;
        this.platform = platform;
    }

    public String getToken() {
        return token;
    }

    public UUID getUserId() {
        return userId;
    }

    public PushPlatform getPlatform() {
        return platform;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
