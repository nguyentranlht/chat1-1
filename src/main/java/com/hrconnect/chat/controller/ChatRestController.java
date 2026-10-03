package com.hrconnect.chat.controller;

import com.hrconnect.chat.dto.ConversationDto;
import com.hrconnect.chat.dto.MessageDto;
import com.hrconnect.chat.dto.MessagePage;
import com.hrconnect.chat.dto.OpenConversationRequest;
import com.hrconnect.chat.dto.ReadRequest;
import com.hrconnect.chat.dto.SendMessageRequest;
import com.hrconnect.chat.port.ChatCurrentUser;
import com.hrconnect.chat.service.ChatService;
import com.hrconnect.chat.websocket.ChatEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST API cua Chat. Gui tin realtime nen dung WebSocket (/app/chat.send);
 * cac API gui/doc/thu hoi o day van day su kien WebSocket nhu binh thuong, tien de test bang Postman.
 */
@RestController
@RequestMapping("/api/v1/chats")
public class ChatRestController {

    private final ChatService chatService;
    private final ChatCurrentUser currentUser;
    private final ChatEventPublisher events;

    public ChatRestController(ChatService chatService, ChatCurrentUser currentUser, ChatEventPublisher events) {
        this.chatService = chatService;
        this.currentUser = currentUser;
        this.events = events;
    }

    /** CHT-02: danh sach hoi thoai cua toi. */
    @GetMapping("/conversations")
    public List<ConversationDto> listConversations() {
        return chatService.listConversations(currentUser.id());
    }

    /** CHT-01: mo (hoac tao) hoi thoai voi mot nguoi. */
    @PostMapping("/conversations")
    public ConversationDto openConversation(@RequestBody OpenConversationRequest request) {
        return chatService.openConversation(currentUser.id(), request.otherUserId());
    }

    /** CHT-05: lich su tin nhan, ?before=&lt;messageId&gt;&amp;limit=30 de tai trang cu hon. */
    @GetMapping("/conversations/{conversationId}/messages")
    public MessagePage history(@PathVariable("conversationId") UUID conversationId,
                               @RequestParam(name = "before", required = false) UUID before,
                               @RequestParam(name = "limit", required = false) Integer limit) {
        return chatService.getHistory(currentUser.id(), conversationId, before, limit);
    }

    /** CHT-05: tim tin theo tu khoa. */
    @GetMapping("/conversations/{conversationId}/messages/search")
    public List<MessageDto> search(@PathVariable("conversationId") UUID conversationId, @RequestParam("q") String keyword) {
        return chatService.search(currentUser.id(), conversationId, keyword);
    }

    /** CHT-03 (ban REST): gui tin vao hoi thoai. */
    @PostMapping("/conversations/{conversationId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto send(@PathVariable("conversationId") UUID conversationId, @RequestBody SendMessageRequest body) {
        SendMessageRequest request = new SendMessageRequest(
                conversationId, null, body.content(), body.type(), body.clientMsgId());
        MessageDto message = chatService.sendMessage(currentUser.id(), request);
        events.messageCreated(message);
        return message;
    }

    /** CHT-04: danh dau da doc toi messageId. */
    @PostMapping("/conversations/{conversationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@PathVariable("conversationId") UUID conversationId, @RequestBody ReadRequest body) {
        events.read(chatService.markRead(currentUser.id(), conversationId, body.messageId()));
    }

    /** CHT-06: thu hoi tin. */
    @PostMapping("/messages/{messageId}/recall")
    public MessageDto recall(@PathVariable("messageId") UUID messageId) {
        MessageDto message = chatService.recall(currentUser.id(), messageId);
        events.messageRecalled(message);
        return message;
    }

    /** CHT-07: an hoi thoai o phia minh. */
    @PostMapping("/conversations/{conversationId}/hide")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hide(@PathVariable("conversationId") UUID conversationId) {
        chatService.hide(currentUser.id(), conversationId);
    }
}
