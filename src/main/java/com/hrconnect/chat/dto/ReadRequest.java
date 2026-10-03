package com.hrconnect.chat.dto;

import java.util.UUID;

/** Danh dau da doc toi tin messageId trong hoi thoai conversationId. */
public record ReadRequest(UUID conversationId, UUID messageId) {
}
