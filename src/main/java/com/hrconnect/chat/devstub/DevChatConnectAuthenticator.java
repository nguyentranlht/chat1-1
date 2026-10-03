package com.hrconnect.chat.devstub;

import com.hrconnect.chat.port.ChatConnectAuthenticator;
import com.hrconnect.chat.port.ChatUserLookup;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * BAN TAM thay Core: khi client STOMP CONNECT, doc header "X-User-Id".
 * Khi ghep: xoa file nay, viet CoreChatConnectAuthenticator doc "Authorization: Bearer <jwt>" va kiem tra bang code cua Core.
 */
@Component
@Profile("chat-dev")
public class DevChatConnectAuthenticator implements ChatConnectAuthenticator {

    private final ChatUserLookup users;

    public DevChatConnectAuthenticator(ChatUserLookup users) {
        this.users = users;
    }

    @Override
    public UUID authenticate(StompHeaderAccessor connectHeaders) {
        return DevHeaderParser.parseUser(connectHeaders.getFirstNativeHeader(DevChatCurrentUser.HEADER), users);
    }
}
