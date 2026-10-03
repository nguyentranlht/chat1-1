package com.hrconnect.chat.devstub;

import com.hrconnect.chat.port.ChatCurrentUser;
import com.hrconnect.chat.port.ChatUserLookup;
import com.hrconnect.chat.service.ChatException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * BAN TAM thay Core: lay user dang goi API tu header "X-User-Id" (khong can dang nhap).
 * Khi ghep: xoa file nay, viet CoreChatCurrentUser doc user id tu SecurityContextHolder.
 */
@Component
@Profile("chat-dev")
public class DevChatCurrentUser implements ChatCurrentUser {

    public static final String HEADER = "X-User-Id";

    private final ChatUserLookup users;

    public DevChatCurrentUser(ChatUserLookup users) {
        this.users = users;
    }

    @Override
    public UUID id() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            throw ChatException.unauthorized("Không có request hiện tại");
        }
        HttpServletRequest request = attributes.getRequest();
        return DevHeaderParser.parseUser(request.getHeader(HEADER), users);
    }
}
