package com.hrconnect.chat.websocket;

import com.hrconnect.chat.port.ChatConnectAuthenticator;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Kiem tra moi khung STOMP tu client:
 * <ul>
 *   <li>CONNECT: xac thuc qua {@link ChatConnectAuthenticator}, gan Principal cho ket noi.</li>
 *   <li>SEND / SUBSCRIBE: bat buoc da xac thuc.</li>
 *   <li>SUBSCRIBE: chi duoc dang ky dia chi rieng cua minh (/user/...), khong nghe len hang doi cua nguoi khac.</li>
 * </ul>
 */
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final ChatConnectAuthenticator authenticator;

    public StompAuthChannelInterceptor(ChatConnectAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command) || StompCommand.STOMP.equals(command)) {
            UUID userId = authenticator.authenticate(accessor);
            accessor.setUser(new ChatPrincipal(userId));
        } else if (StompCommand.SEND.equals(command) || StompCommand.SUBSCRIBE.equals(command)) {
            if (accessor.getUser() == null) {
                throw new MessageDeliveryException("Kết nối WebSocket chưa xác thực");
            }
            if (StompCommand.SUBSCRIBE.equals(command)) {
                String destination = accessor.getDestination();
                if (destination == null || !destination.startsWith("/user/")) {
                    throw new MessageDeliveryException("Chỉ được đăng ký địa chỉ /user/...");
                }
            }
        }
        return message;
    }
}
