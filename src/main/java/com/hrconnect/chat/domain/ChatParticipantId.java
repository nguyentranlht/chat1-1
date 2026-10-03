package com.hrconnect.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** Khoa chinh ghep (conversation_id, user_id) cua bang chat_participants. */
@Embeddable
public class ChatParticipantId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    protected ChatParticipantId() {
        // JPA
    }

    public ChatParticipantId(UUID conversationId, UUID userId) {
        this.conversationId = conversationId;
        this.userId = userId;
    }

    public UUID getConversationId() {
        return conversationId;
    }

    public UUID getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChatParticipantId other)) {
            return false;
        }
        return Objects.equals(conversationId, other.conversationId) && Objects.equals(userId, other.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationId, userId);
    }
}
