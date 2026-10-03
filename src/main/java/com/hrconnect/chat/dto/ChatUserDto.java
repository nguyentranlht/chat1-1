package com.hrconnect.chat.dto;

import com.hrconnect.chat.port.ChatUserInfo;

import java.util.UUID;

public record ChatUserDto(UUID id, String fullName, String avatarUrl) {

    public static final String UNKNOWN_NAME = "Người dùng không tồn tại";

    public static ChatUserDto from(ChatUserInfo info) {
        return new ChatUserDto(info.id(), info.fullName(), info.avatarUrl());
    }

    public static ChatUserDto unknown(UUID id) {
        return new ChatUserDto(id, UNKNOWN_NAME, null);
    }
}
