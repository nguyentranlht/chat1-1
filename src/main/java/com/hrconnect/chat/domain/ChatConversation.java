package com.hrconnect.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Hoi thoai 1-1 giua hai nguoi. Luon co userAId < userBId (xem {@link UserPair}).
 * Dong moi duoc tao bang native INSERT ... ON CONFLICT trong repository
 * de khong bi trung khi hai nguoi cung bam "Nhan tin" mot luc.
 */
@Entity
@Table(name = "chat_conversations")
public class ChatConversation {

    @Id
    private UUID id;

    @Column(name = "user_a_id", nullable = false, updatable = false)
    private UUID userAId;

    @Column(name = "user_b_id", nullable = false, updatable = false)
    private UUID userBId;

    @Column(name = "last_message_id")
    private UUID lastMessageId;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    protected ChatConversation() {
        // JPA
    }

    /** Dung trong test. Trong ung dung, hoi thoai duoc tao qua repository. */
    public ChatConversation(UUID id, UserPair pair) {
        this.id = id;
        this.userAId = pair.userA();
        this.userBId = pair.userB();
    }

    public boolean hasParticipant(UUID userId) {
        return userAId.equals(userId) || userBId.equals(userId);
    }

    /** Nguoi con lai trong hoi thoai. */
    public UUID otherUser(UUID me) {
        if (userAId.equals(me)) {
            return userBId;
        }
        if (userBId.equals(me)) {
            return userAId;
        }
        throw new IllegalArgumentException("User " + me + " khong thuoc hoi thoai " + id);
    }

    /** Cap nhat tin cuoi de sap xep danh sach hoi thoai. */
    public void onNewMessage(ChatMessage message) {
        this.lastMessageId = message.getId();
        this.lastMessageAt = message.getCreatedAt();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserAId() {
        return userAId;
    }

    public UUID getUserBId() {
        return userBId;
    }

    public UUID getLastMessageId() {
        return lastMessageId;
    }

    public Instant getLastMessageAt() {
        return lastMessageAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
