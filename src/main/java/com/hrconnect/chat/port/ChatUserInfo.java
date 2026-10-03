package com.hrconnect.chat.port;

import java.util.UUID;

/** Thong tin toi thieu ve mot nguoi dung ma Chat can (lay tu Core). */
public record ChatUserInfo(UUID id, String fullName, String avatarUrl, boolean active) {
}
