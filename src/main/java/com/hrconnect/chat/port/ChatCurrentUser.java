package com.hrconnect.chat.port;

import java.util.UUID;

/**
 * Ai dang goi REST API cua Chat.
 *
 * <p>Hien tai: ban tam {@code devstub.DevChatCurrentUser} doc header X-User-Id.
 * Khi ghep voi Core: doc user id tu SecurityContextHolder (JWT do Core cap).
 */
public interface ChatCurrentUser {

    /** Id nguoi dang dang nhap. Nem ChatException(UNAUTHORIZED) neu chua dang nhap. */
    UUID id();
}
