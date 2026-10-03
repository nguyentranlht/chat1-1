package com.hrconnect.chat.dto;

import com.hrconnect.chat.domain.ChatMessage;
import com.hrconnect.chat.domain.MessageType;

import java.time.Instant;
import java.util.UUID;

/**
 * Tin nhan tra ve cho client. Tin da thu hoi thi content = null va recalled = true.
 * clientMsgId: ma tam do client tu sinh khi gui, server tra lai de client khop voi tin dang hien "dang gui".
 */
public record MessageDto(
        UUID id,
        UUID conversationId,
        UUID senderId,
        UUID recipientId,
        String content,
        MessageType type,
        boolean recalled,
        Instant createdAt,
        String clientMsgId) {

    public static MessageDto from(ChatMessage m, UUID recipientId, String clientMsgId) {
        return new MessageDto(
                m.getId(),
                m.getConversationId(),
                m.getSenderId(),
                recipientId,
                m.isRecalled() ? null : m.getContent(),
                m.getType(),
                m.isRecalled(),
                m.getCreatedAt(),
                clientMsgId);
    }
}
