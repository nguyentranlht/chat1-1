package com.hrconnect.chat.devstub;

import com.hrconnect.chat.port.ChatUserInfo;

import java.util.List;
import java.util.UUID;

/**
 * Danh sach user GIA de test khi Core chua xong. XOA ca thu muc devstub khi ghep voi Core.
 * Copy cac id nay vao header X-User-Id khi goi API bang Postman.
 */
final class DevChatUsers {

    static final List<ChatUserInfo> ALL = List.of(
            new ChatUserInfo(UUID.fromString("11111111-1111-1111-1111-111111111111"), "Nguyễn Văn An", null, true),
            new ChatUserInfo(UUID.fromString("22222222-2222-2222-2222-222222222222"), "Trần Thị Bình", null, true),
            new ChatUserInfo(UUID.fromString("33333333-3333-3333-3333-333333333333"), "Lê Văn Cường", null, true),
            new ChatUserInfo(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), "Phạm Thị Dung", null, true),
            // Tai khoan bi khoa: dung de test "khong nhan tin duoc cho tai khoan bi khoa"
            new ChatUserInfo(UUID.fromString("99999999-9999-9999-9999-999999999999"), "Tài khoản bị khoá", null, false));

    private DevChatUsers() {
    }
}
