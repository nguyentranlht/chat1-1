package com.hrconnect.chat.dto;

import com.hrconnect.chat.domain.MessageType;

import java.util.UUID;

/**
 * Gui tin nhan. Truyen conversationId (da co hoi thoai) HOAC recipientId (nhan lan dau, server tu tao hoi thoai).
 * type bo trong thi mac dinh TEXT.
 */
public record SendMessageRequest(
        UUID conversationId,
        UUID recipientId,
        String content,
        MessageType type,
        String clientMsgId) {
}
