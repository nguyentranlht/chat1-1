package com.hrconnect.chat.dto;

import java.util.UUID;

/** Gui cho notifyUserId: nguoi readerId da doc toi tin lastReadMessageId. */
public record ReadEvent(UUID conversationId, UUID readerId, UUID lastReadMessageId, UUID notifyUserId) {
}
