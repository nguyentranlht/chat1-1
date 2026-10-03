package com.hrconnect.chat.dto;

import java.util.UUID;

/** Gui cho notifyUserId: nguoi userId dang nhap (typing = true) hoac da dung (false). */
public record TypingEvent(UUID conversationId, UUID userId, boolean typing, UUID notifyUserId) {
}
