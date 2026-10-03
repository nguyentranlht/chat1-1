package com.hrconnect.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "chat_messages")
public class ChatMessage implements Persistable<UUID> {

    /** Noi dung luu lai sau khi thu hoi (cot content la NOT NULL). */
    public static final String RECALLED_CONTENT = "";

    @Id
    private UUID id;

    @Column(name = "conversation_id", nullable = false, updatable = false)
    private UUID conversationId;

    @Column(name = "sender_id", nullable = false, updatable = false)
    private UUID senderId;

    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 10)
    private MessageType type;

    @Column(name = "recalled_at")
    private Instant recalledAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** id duoc tao trong Java, nen can co nay de Spring Data biet day la ban ghi moi (INSERT thang, khong SELECT truoc). */
    @Transient
    private boolean isNew = true;

    protected ChatMessage() {
        // JPA
    }

    public ChatMessage(UUID conversationId, UUID senderId, String content, MessageType type, Instant now) {
        this.id = UUID.randomUUID();
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        // PostgreSQL luu toi micro giay; cat bot de gia tri trong bo nho khop voi DB
        this.createdAt = now.truncatedTo(ChronoUnit.MICROS);
    }

    public boolean isRecalled() {
        return recalledAt != null;
    }

    public void recall(Instant at) {
        this.recalledAt = at.truncatedTo(ChronoUnit.MICROS);
        this.content = RECALLED_CONTENT;
    }

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    @Override
    public UUID getId() {
        return id;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public MessageType getType() {
        return type;
    }

    public Instant getRecalledAt() {
        return recalledAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
