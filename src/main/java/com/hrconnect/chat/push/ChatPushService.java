package com.hrconnect.chat.push;

import com.hrconnect.chat.domain.ChatParticipant;
import com.hrconnect.chat.domain.ChatParticipantId;
import com.hrconnect.chat.domain.ChatPushToken;
import com.hrconnect.chat.domain.PushPlatform;
import com.hrconnect.chat.dto.MessageDto;
import com.hrconnect.chat.port.ChatUserInfo;
import com.hrconnect.chat.port.ChatUserLookup;
import com.hrconnect.chat.repository.ChatParticipantRepository;
import com.hrconnect.chat.repository.ChatPushTokenRepository;
import com.hrconnect.chat.service.ChatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Dang ky FCM token va day thong bao "co tin nhan moi" toi nguoi nhan. */
@Service
public class ChatPushService {

    private static final Logger log = LoggerFactory.getLogger(ChatPushService.class);

    public static final int MAX_TOKEN_LENGTH = 512;
    public static final int MAX_BODY_LENGTH = 120;

    private final ChatPushTokenRepository tokens;
    private final ChatParticipantRepository participants;
    private final ChatUserLookup users;
    private final ChatPushSender sender;

    public ChatPushService(ChatPushTokenRepository tokens,
                           ChatParticipantRepository participants,
                           ChatUserLookup users,
                           ChatPushSender sender) {
        this.tokens = tokens;
        this.participants = participants;
        this.users = users;
        this.sender = sender;
    }

    @Transactional
    public void register(UUID me, String token, PushPlatform platform) {
        tokens.upsert(validateToken(token), me, (platform == null ? PushPlatform.WEB : platform).name());
    }

    @Transactional
    public void unregister(UUID me, String token) {
        tokens.deleteOwned(validateToken(token), me);
    }

    /**
     * Bao cho nguoi nhan co tin moi. Chay o luong rieng de khong lam cham viec gui tin;
     * loi o day chi ghi log, khong anh huong toi tin nhan da gui.
     */
    @Async
    @Transactional
    public void notifyNewMessage(MessageDto message) {
        try {
            UUID recipient = message.recipientId();
            boolean muted = participants.findById(new ChatParticipantId(message.conversationId(), recipient))
                    .map(ChatParticipant::isMuted)
                    .orElse(false);
            if (muted) {
                return;
            }
            List<String> targets = tokens.findByUserId(recipient).stream().map(ChatPushToken::getToken).toList();
            if (targets.isEmpty()) {
                return;
            }
            String title = users.findById(message.senderId()).map(ChatUserInfo::fullName).orElse("Tin nhắn mới");
            PushNotification notification = new PushNotification(
                    title,
                    preview(message.content()),
                    message.conversationId().toString(),
                    Map.of(
                            "kind", "chat.message",
                            "conversationId", message.conversationId().toString(),
                            "messageId", message.id().toString(),
                            "senderId", message.senderId().toString(),
                            "recipientId", recipient.toString()));

            Set<String> invalid = sender.send(targets, notification);
            if (!invalid.isEmpty()) {
                tokens.deleteAllByTokens(invalid);
                log.debug("Đã xoá {} FCM token hết hạn của user {}", invalid.size(), recipient);
            }
        } catch (RuntimeException e) {
            log.warn("Không gửi được thông báo đẩy cho tin {}", message.id(), e);
        }
    }

    static String preview(String content) {
        if (content == null) {
            return "";
        }
        String oneLine = content.strip().replaceAll("\\s+", " ");
        return oneLine.length() <= MAX_BODY_LENGTH ? oneLine : oneLine.substring(0, MAX_BODY_LENGTH - 1) + "…";
    }

    private static String validateToken(String token) {
        if (token == null || token.isBlank()) {
            throw ChatException.badRequest("Thiếu token");
        }
        if (token.length() > MAX_TOKEN_LENGTH) {
            throw ChatException.badRequest("Token tối đa " + MAX_TOKEN_LENGTH + " ký tự");
        }
        return token.strip();
    }
}
