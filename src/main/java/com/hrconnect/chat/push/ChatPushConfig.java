package com.hrconnect.chat.push;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

@Configuration
@EnableAsync
@EnableConfigurationProperties(ChatPushProperties.class)
public class ChatPushConfig {

    private static final Logger log = LoggerFactory.getLogger(ChatPushConfig.class);

    /** Ten rieng de khong dung voi FirebaseApp cua module khac (neu co). */
    private static final String FIREBASE_APP_NAME = "hrconnect-chat";

    /** Co file service account thi gui that qua Firebase, khong thi chi ghi log. */
    @Bean
    public ChatPushSender chatPushSender(ChatPushProperties props) throws IOException {
        if (!props.serverEnabled()) {
            log.info("Chưa cấu hình chat.push.credentials-path: tắt gửi thông báo đẩy (push)");
            return (tokens, notification) -> {
                log.debug("[push tắt] Bỏ qua thông báo '{}' tới {} token", notification.title(), tokens.size());
                return Set.of();
            };
        }
        FirebaseApp app;
        try (InputStream in = Files.newInputStream(Path.of(props.credentialsPath()))) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(in))
                    .build();
            app = FirebaseApp.getApps().stream()
                    .filter(a -> a.getName().equals(FIREBASE_APP_NAME))
                    .findFirst()
                    .orElseGet(() -> FirebaseApp.initializeApp(options, FIREBASE_APP_NAME));
        }
        log.info("Đã bật gửi thông báo đẩy qua Firebase");
        return new FirebaseChatPushSender(FirebaseMessaging.getInstance(app));
    }
}
