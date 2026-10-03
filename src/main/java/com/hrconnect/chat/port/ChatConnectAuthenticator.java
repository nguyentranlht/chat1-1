package com.hrconnect.chat.port;

import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.UUID;

/**
 * Xac thuc khi client mo ket noi WebSocket (lenh STOMP CONNECT).
 *
 * <p>Hien tai: ban tam {@code devstub.DevChatConnectAuthenticator} doc header X-User-Id.
 * Khi ghep voi Core: doc header "Authorization: Bearer &lt;jwt&gt;" va kiem tra JWT bang code cua Core.
 */
public interface ChatConnectAuthenticator {

    /** Tra ve user id neu hop le; nem exception neu khong hop le (ket noi se bi tu choi). */
    UUID authenticate(StompHeaderAccessor connectHeaders);
}
