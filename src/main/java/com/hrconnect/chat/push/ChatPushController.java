package com.hrconnect.chat.push;

import com.hrconnect.chat.domain.PushPlatform;
import com.hrconnect.chat.port.ChatCurrentUser;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** API dang ky nhan thong bao day (push) cho Chat. */
@RestController
@RequestMapping("/api/v1/chats/push")
public class ChatPushController {

    private final ChatPushService pushService;
    private final ChatCurrentUser currentUser;
    private final ChatPushProperties props;

    public ChatPushController(ChatPushService pushService, ChatCurrentUser currentUser, ChatPushProperties props) {
        this.pushService = pushService;
        this.currentUser = currentUser;
        this.props = props;
    }

    /** Cau hinh Firebase cho trinh duyet (khong bi mat). enabled = false thi client an nut bat thong bao. */
    @GetMapping("/config")
    public WebConfig config() {
        return props.webEnabled() ? new WebConfig(true, props.web()) : new WebConfig(false, null);
    }

    /** Luu FCM token cua trinh duyet / thiet bi hien tai. Goi lai moi lan mo app (token co the doi). */
    @PostMapping("/tokens")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void register(@RequestBody TokenRequest body) {
        pushService.register(currentUser.id(), body.token(), body.platform());
    }

    /** Huy nhan thong bao tren trinh duyet / thiet bi nay (vd. khi dang xuat). */
    @DeleteMapping("/tokens")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unregister(@RequestBody TokenRequest body) {
        pushService.unregister(currentUser.id(), body.token());
    }

    public record TokenRequest(String token, PushPlatform platform) {
    }

    public record WebConfig(boolean enabled, ChatPushProperties.Web firebase) {
    }
}
