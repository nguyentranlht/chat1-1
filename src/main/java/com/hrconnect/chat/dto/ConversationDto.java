package com.hrconnect.chat.dto;

import java.time.Instant;
import java.util.UUID;

/** Mot dong trong danh sach hoi thoai. */
public record ConversationDto(
        UUID id,
        ChatUserDto otherUser,
        MessageDto lastMessage,
        Instant lastMessageAt,
        long unreadCount,
        UUID otherLastReadMessageId) {
}
