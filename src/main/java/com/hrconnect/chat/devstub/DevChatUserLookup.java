package com.hrconnect.chat.devstub;

import com.hrconnect.chat.port.ChatUserInfo;
import com.hrconnect.chat.port.ChatUserLookup;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/** BAN TAM thay Core. Khi ghep: xoa file nay, viet CoreChatUserLookup goi UserService cua Core. */
@Component
@Profile("chat-dev")
public class DevChatUserLookup implements ChatUserLookup {

    private final Map<UUID, ChatUserInfo> users = DevChatUsers.ALL.stream()
            .collect(Collectors.toMap(ChatUserInfo::id, Function.identity()));

    @Override
    public Optional<ChatUserInfo> findById(UUID userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Map<UUID, ChatUserInfo> findByIds(Collection<UUID> userIds) {
        return userIds.stream()
                .filter(users::containsKey)
                .distinct()
                .collect(Collectors.toMap(Function.identity(), users::get));
    }
}
