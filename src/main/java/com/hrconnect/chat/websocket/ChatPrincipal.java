package com.hrconnect.chat.websocket;

import java.security.Principal;
import java.util.UUID;

/** Nguoi dung cua mot ket noi WebSocket. getName() = user id, Spring dung no de gui tin toi /user/... */
public record ChatPrincipal(UUID userId) implements Principal {

    @Override
    public String getName() {
        return userId.toString();
    }

    /** Lay user id tu Principal bat ky (ke ca Principal do Spring Security/Core tao, mien la getName() = user id). */
    public static UUID userIdOf(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("Kết nối WebSocket chưa xác thực");
        }
        if (principal instanceof ChatPrincipal chatPrincipal) {
            return chatPrincipal.userId();
        }
        return UUID.fromString(principal.getName());
    }
}
