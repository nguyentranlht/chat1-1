package com.hrconnect.chat.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket dung giao thuc STOMP.
 * <ul>
 *   <li>Client ket noi: ws://host:8080/ws</li>
 *   <li>Client gui len: /app/chat.send, /app/chat.read, /app/chat.typing, /app/chat.recall</li>
 *   <li>Client nhan ve: /user/queue/chat.* (xem {@link ChatEventPublisher})</li>
 * </ul>
 *
 * <p>Luu y khi ghep code: chi duoc co MOT cau hinh @EnableWebSocketMessageBroker trong ca app.
 * Neu module khac (vd. thong bao) cung can WebSocket, dung chung cau hinh nay.
 */
@Configuration
@EnableWebSocketMessageBroker
public class ChatWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor authInterceptor;
    private final String[] allowedOrigins;

    public ChatWebSocketConfig(StompAuthChannelInterceptor authInterceptor,
                               @Value("${chat.ws.allowed-origins:*}") String[] allowedOrigins) {
        this.authInterceptor = authInterceptor;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns(allowedOrigins);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }
}
