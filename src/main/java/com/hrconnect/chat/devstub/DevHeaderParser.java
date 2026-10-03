package com.hrconnect.chat.devstub;

import com.hrconnect.chat.port.ChatUserInfo;
import com.hrconnect.chat.port.ChatUserLookup;
import com.hrconnect.chat.service.ChatException;

import java.util.UUID;

final class DevHeaderParser {

    private DevHeaderParser() {
    }

    static UUID parseUser(String headerValue, ChatUserLookup users) {
        if (headerValue == null || headerValue.isBlank()) {
            throw ChatException.unauthorized("Thiếu header X-User-Id (chế độ chat-dev)");
        }
        UUID id;
        try {
            id = UUID.fromString(headerValue.strip());
        } catch (IllegalArgumentException e) {
            throw ChatException.unauthorized("X-User-Id không phải UUID hợp lệ");
        }
        ChatUserInfo info = users.findById(id)
                .orElseThrow(() -> ChatException.unauthorized("Không có user " + id + " trong danh sách user giả"));
        if (!info.active()) {
            throw ChatException.unauthorized("Tài khoản đã bị khoá");
        }
        return id;
    }
}
