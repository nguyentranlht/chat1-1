package com.hrconnect.chat.websocket;

import com.hrconnect.chat.dto.ChatErrorDto;
import com.hrconnect.chat.dto.MessageDto;
import com.hrconnect.chat.dto.ReadRequest;
import com.hrconnect.chat.dto.RecallRequest;
import com.hrconnect.chat.dto.SendMessageRequest;
import com.hrconnect.chat.dto.TypingRequest;
import com.hrconnect.chat.service.ChatException;
import com.hrconnect.chat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/** Nhan khung STOMP tu client (dia chi /app/chat.*). */
@Controller
public class ChatStompController {

    private static final Logger log = LoggerFactory.getLogger(ChatStompController.class);

    private final ChatService chatService;
    private final ChatEventPublisher events;

    public ChatStompController(ChatService chatService, ChatEventPublisher events) {
        this.chatService = chatService;
        this.events = events;
    }

    /** CHT-03: gui tin. Payload: {conversationId | recipientId, content, type?, clientMsgId?} */
    @MessageMapping("/chat.send")
    public void send(@Payload SendMessageRequest request, Principal principal) {
        MessageDto message = chatService.sendMessage(ChatPrincipal.userIdOf(principal), request);
        events.messageCreated(message);
    }

    /** CHT-04: da doc. Payload: {conversationId, messageId} */
    @MessageMapping("/chat.read")
    public void read(@Payload ReadRequest request, Principal principal) {
        events.read(chatService.markRead(ChatPrincipal.userIdOf(principal), request.conversationId(), request.messageId()));
    }

    /** CHT-08: dang nhap. Payload: {conversationId, typing} */
    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingRequest request, Principal principal) {
        events.typing(chatService.typing(ChatPrincipal.userIdOf(principal), request.conversationId(), request.typing()));
    }

    /** CHT-06: thu hoi. Payload: {messageId} */
    @MessageMapping("/chat.recall")
    public void recall(@Payload RecallRequest request, Principal principal) {
        events.messageRecalled(chatService.recall(ChatPrincipal.userIdOf(principal), request.messageId()));
    }

    @MessageExceptionHandler(ChatException.class)
    @SendToUser(destinations = ChatEventPublisher.ERRORS, broadcast = false)
    public ChatErrorDto handleChatError(ChatException e) {
        return new ChatErrorDto(e.getCode().name(), e.getMessage());
    }

    @MessageExceptionHandler(Exception.class)
    @SendToUser(destinations = ChatEventPublisher.ERRORS, broadcast = false)
    public ChatErrorDto handleUnexpected(Exception e) {
        log.error("Lỗi khi xử lý tin nhắn WebSocket", e);
        return new ChatErrorDto("INTERNAL_ERROR", "Có lỗi xảy ra, vui lòng thử lại");
    }
}
