package com.hrconnect.chat.devstub;

import com.hrconnect.chat.dto.ChatUserDto;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** BAN TAM: danh sach user gia cho trang chat-test.html. Xoa khi ghep. */
@RestController
@Profile("chat-dev")
public class DevChatUserController {

    @GetMapping("/api/v1/chats/dev/users")
    public List<DevUser> users() {
        return DevChatUsers.ALL.stream()
                .map(u -> new DevUser(ChatUserDto.from(u), u.active()))
                .toList();
    }

    public record DevUser(ChatUserDto user, boolean active) {
    }
}
