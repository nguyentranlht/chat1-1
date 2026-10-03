package com.hrconnect.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Trang thai rieng cua mot nguoi trong mot hoi thoai: da doc toi dau, da an chua, co tat thong bao khong. */
@Entity
@Table(name = "chat_participants")
public class ChatParticipant {

    @EmbeddedId
    private ChatParticipantId id;

    @Column(name = "last_read_message_id")
    private UUID lastReadMessageId;

    @Column(name = "hidden_at")
    private Instant hiddenAt;

    @Column(name = "is_muted", nullable = false)
    private boolean muted;

    protected ChatParticipant() {
        // JPA
    }

    /** Dung trong test. Trong ung dung, dong nay duoc tao qua repository. */
    public ChatParticipant(ChatParticipantId id) {
        this.id = id;
    }

    /**
     * Hoi thoai bi an chi khi duoc an SAU tin nhan cuoi.
     * Co tin moi den sau thoi diem an thi hoi thoai tu hien lai.
     */
    public boolean isHiddenFor(Instant lastMessageAt) {
        if (hiddenAt == null) {
            return false;
        }
        return lastMessageAt == null || !lastMessageAt.isAfter(hiddenAt);
    }

    public void markRead(UUID messageId) {
        this.lastReadMessageId = messageId;
    }

    public void hide(Instant at) {
        this.hiddenAt = at;
    }

    public ChatParticipantId getId() {
        return id;
    }

    public UUID getConversationId() {
        return id.getConversationId();
    }

    public UUID getUserId() {
        return id.getUserId();
    }

    public UUID getLastReadMessageId() {
        return lastReadMessageId;
    }

    public Instant getHiddenAt() {
        return hiddenAt;
    }

    public boolean isMuted() {
        return muted;
    }
}
