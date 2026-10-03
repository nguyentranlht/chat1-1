package com.hrconnect.chat.websocket;

import com.hrconnect.chat.dto.MessageDto;
import com.hrconnect.chat.dto.ReadEvent;
import com.hrconnect.chat.dto.TypingEvent;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Day su kien realtime toi dung nguoi qua WebSocket.
 * Client dang ky (SUBSCRIBE) cac dia chi:
 * <ul>
 *   <li>/user/queue/chat.messages  – tin moi (ca nguoi gui va nguoi nhan deu nhan)</li>
 *   <li>/user/queue/chat.recalled  – tin bi thu hoi</li>
 *   <li>/user/queue/chat.read      – nguoi kia da doc</li>
 *   <li>/user/queue/chat.typing    – nguoi kia dang nhap</li>
 *   <li>/user/queue/chat.errors    – loi khi gui qua WebSocket</li>
 * </ul>
 */
@Component
public class ChatEventPublisher {

    public static final String MESSAGES = "/queue/chat.messages";
    public static final String RECALLED = "/queue/chat.recalled";
    public static final String READ = "/queue/chat.read";
    public static final String TYPING = "/queue/chat.typing";
    public static final String ERRORS = "/queue/chat.errors";

    private final SimpMessageSendingOperations messaging;

    public ChatEventPublisher(SimpMessageSendingOperations messaging) {
        this.messaging = messaging;
    }

    public void messageCreated(MessageDto message) {
        sendToBoth(message, MESSAGES);
    }

    public void messageRecalled(MessageDto message) {
        sendToBoth(message, RECALLED);
    }

    public void read(ReadEvent event) {
        if (event != null) {
            send(event.notifyUserId(), READ, event);
        }
    }

    public void typing(TypingEvent event) {
        send(event.notifyUserId(), TYPING, event);
    }

    private void sendToBoth(MessageDto message, String destination) {
        send(message.senderId(), destination, message);
        send(message.recipientId(), destination, message);
    }

    private void send(UUID userId, String destination, Object payload) {
        messaging.convertAndSendToUser(userId.toString(), destination, payload);
    }
}
