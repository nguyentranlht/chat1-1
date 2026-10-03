package com.hrconnect.chat.service;

import com.hrconnect.chat.domain.ChatConversation;
import com.hrconnect.chat.domain.ChatMessage;
import com.hrconnect.chat.domain.ChatParticipant;
import com.hrconnect.chat.domain.ChatParticipantId;
import com.hrconnect.chat.domain.MessageType;
import com.hrconnect.chat.domain.UserPair;
import com.hrconnect.chat.dto.ChatUserDto;
import com.hrconnect.chat.dto.ConversationDto;
import com.hrconnect.chat.dto.MessageDto;
import com.hrconnect.chat.dto.MessagePage;
import com.hrconnect.chat.dto.ReadEvent;
import com.hrconnect.chat.dto.SendMessageRequest;
import com.hrconnect.chat.dto.TypingEvent;
import com.hrconnect.chat.port.ChatUserInfo;
import com.hrconnect.chat.port.ChatUserLookup;
import com.hrconnect.chat.repository.ChatConversationRepository;
import com.hrconnect.chat.repository.ChatMessageRepository;
import com.hrconnect.chat.repository.ChatParticipantRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Toan bo nghiep vu Chat 1-1. Chi phu thuoc vao repository cua Chat va interface {@link ChatUserLookup},
 * khong import bat ky class nao cua Core, nen khi ghep code khong phai sua file nay.
 */
@Service
@Transactional
public class ChatService {

    public static final int DEFAULT_PAGE_SIZE = 30;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int MAX_CONTENT_LENGTH = 4000;
    public static final int MAX_CLIENT_MSG_ID_LENGTH = 64;
    public static final int MAX_SEARCH_LENGTH = 100;
    public static final int SEARCH_LIMIT = 50;
    public static final Duration RECALL_WINDOW = Duration.ofHours(24);

    private final ChatConversationRepository conversations;
    private final ChatParticipantRepository participants;
    private final ChatMessageRepository messages;
    private final ChatUserLookup users;
    private final Clock clock;

    public ChatService(ChatConversationRepository conversations,
                       ChatParticipantRepository participants,
                       ChatMessageRepository messages,
                       ChatUserLookup users,
                       @Qualifier("chatClock") Clock clock) {
        this.conversations = conversations;
        this.participants = participants;
        this.messages = messages;
        this.users = users;
        this.clock = clock;
    }

    // ------------------------------------------------------------------ CHT-01: mo hoi thoai

    /** Tim hoi thoai voi otherUserId, chua co thi tao. Goi nhieu lan van ra cung mot hoi thoai. */
    public ConversationDto openConversation(UUID me, UUID otherUserId) {
        ChatConversation conversation = getOrCreateConversation(me, otherUserId);
        return toConversationDtos(me, List.of(conversation)).get(0);
    }

    // ------------------------------------------------------------------ CHT-02: danh sach hoi thoai

    /** Cac hoi thoai co it nhat mot tin nhan va khong bi an, moi nhat len dau. */
    @Transactional(readOnly = true)
    public List<ConversationDto> listConversations(UUID me) {
        Map<UUID, ChatParticipant> mine = participants.findByIdUserId(me).stream()
                .collect(Collectors.toMap(ChatParticipant::getConversationId, Function.identity()));
        if (mine.isEmpty()) {
            return List.of();
        }
        List<ChatConversation> visible = conversations.findAllById(mine.keySet()).stream()
                .filter(c -> c.getLastMessageAt() != null)
                .filter(c -> !mine.get(c.getId()).isHiddenFor(c.getLastMessageAt()))
                .sorted(Comparator.comparing(ChatConversation::getLastMessageAt).reversed())
                .toList();
        return toConversationDtos(me, visible);
    }

    // ------------------------------------------------------------------ CHT-03: gui tin

    public MessageDto sendMessage(UUID me, SendMessageRequest request) {
        if (request == null) {
            throw ChatException.badRequest("Thiếu nội dung yêu cầu");
        }
        String content = validateContent(request.content());
        MessageType type = request.type() == null ? MessageType.TEXT : request.type();
        if (type != MessageType.TEXT) {
            throw ChatException.badRequest("Hiện chỉ hỗ trợ tin nhắn văn bản");
        }
        String clientMsgId = request.clientMsgId();
        if (clientMsgId != null && clientMsgId.length() > MAX_CLIENT_MSG_ID_LENGTH) {
            throw ChatException.badRequest("clientMsgId tối đa " + MAX_CLIENT_MSG_ID_LENGTH + " ký tự");
        }

        ChatConversation conversation;
        if (request.conversationId() != null) {
            conversation = requireConversationOf(me, request.conversationId());
            requireActiveUser(conversation.otherUser(me));
        } else if (request.recipientId() != null) {
            conversation = getOrCreateConversation(me, request.recipientId());
        } else {
            throw ChatException.badRequest("Cần conversationId hoặc recipientId");
        }

        ChatMessage message = new ChatMessage(conversation.getId(), me, content, type, clock.instant());
        messages.save(message);
        conversation.onNewMessage(message);
        // Nguoi gui coi nhu da doc toi tin cua chinh minh
        participants.findById(new ChatParticipantId(conversation.getId(), me))
                .ifPresent(p -> p.markRead(message.getId()));

        return MessageDto.from(message, conversation.otherUser(me), clientMsgId);
    }

    // ------------------------------------------------------------------ CHT-04: da doc

    /**
     * Danh dau da doc toi messageId. Tra ve su kien de bao cho nguoi kia,
     * hoac null neu khong co gi thay doi (tin nay cu hon tin da doc truoc do).
     */
    public ReadEvent markRead(UUID me, UUID conversationId, UUID messageId) {
        ChatConversation conversation = requireConversationOf(me, conversationId);
        if (messageId == null) {
            throw ChatException.badRequest("Thiếu messageId");
        }
        ChatMessage target = messages.findById(messageId)
                .filter(m -> m.getConversationId().equals(conversationId))
                .orElseThrow(() -> ChatException.notFound("Không tìm thấy tin nhắn trong hội thoại"));
        ChatParticipant myself = requireParticipant(conversationId, me);

        if (myself.getLastReadMessageId() != null) {
            ChatMessage current = messages.findById(myself.getLastReadMessageId()).orElse(null);
            if (current != null && !isNewer(target, current)) {
                return null;
            }
        }
        myself.markRead(target.getId());
        return new ReadEvent(conversationId, me, target.getId(), conversation.otherUser(me));
    }

    // ------------------------------------------------------------------ CHT-05: lich su va tim kiem

    @Transactional(readOnly = true)
    public MessagePage getHistory(UUID me, UUID conversationId, UUID beforeId, Integer limit) {
        ChatConversation conversation = requireConversationOf(me, conversationId);
        int size = limit == null ? DEFAULT_PAGE_SIZE : Math.max(1, Math.min(limit, MAX_PAGE_SIZE));

        List<ChatMessage> rows;
        if (beforeId == null) {
            rows = messages.findLatest(conversationId, size + 1);
        } else {
            messages.findById(beforeId)
                    .filter(m -> m.getConversationId().equals(conversationId))
                    .orElseThrow(() -> ChatException.badRequest("Tham số before không hợp lệ"));
            rows = messages.findBefore(conversationId, beforeId, size + 1);
        }

        boolean hasMore = rows.size() > size;
        List<ChatMessage> page = new ArrayList<>(hasMore ? rows.subList(0, size) : rows);
        UUID nextBefore = hasMore ? page.get(page.size() - 1).getId() : null;
        // Query tra ve tu moi den cu; dao lai de client hien thi luon
        Collections.reverse(page);

        List<MessageDto> items = page.stream()
                .map(m -> MessageDto.from(m, conversation.otherUser(m.getSenderId()), null))
                .toList();
        return new MessagePage(items, nextBefore);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> search(UUID me, UUID conversationId, String keyword) {
        ChatConversation conversation = requireConversationOf(me, conversationId);
        if (keyword == null || keyword.isBlank()) {
            throw ChatException.badRequest("Nhập từ khoá cần tìm");
        }
        String q = keyword.strip();
        if (q.length() > MAX_SEARCH_LENGTH) {
            throw ChatException.badRequest("Từ khoá tối đa " + MAX_SEARCH_LENGTH + " ký tự");
        }
        return messages.search(conversationId, escapeLike(q), SEARCH_LIMIT).stream()
                .map(m -> MessageDto.from(m, conversation.otherUser(m.getSenderId()), null))
                .toList();
    }

    // ------------------------------------------------------------------ CHT-06: thu hoi

    public MessageDto recall(UUID me, UUID messageId) {
        if (messageId == null) {
            throw ChatException.badRequest("Thiếu messageId");
        }
        ChatMessage message = messages.findById(messageId)
                .orElseThrow(() -> ChatException.notFound("Không tìm thấy tin nhắn"));
        ChatConversation conversation = requireConversationOf(me, message.getConversationId());
        if (!message.getSenderId().equals(me)) {
            throw ChatException.forbidden("Chỉ người gửi mới thu hồi được tin nhắn");
        }
        if (!message.isRecalled()) {
            Instant now = clock.instant();
            if (Duration.between(message.getCreatedAt(), now).compareTo(RECALL_WINDOW) > 0) {
                throw ChatException.badRequest("Chỉ thu hồi được tin nhắn trong vòng 24 giờ");
            }
            message.recall(now);
        }
        return MessageDto.from(message, conversation.otherUser(me), null);
    }

    // ------------------------------------------------------------------ CHT-07: an hoi thoai

    public void hide(UUID me, UUID conversationId) {
        requireConversationOf(me, conversationId);
        requireParticipant(conversationId, me).hide(clock.instant());
    }

    // ------------------------------------------------------------------ CHT-08: dang nhap

    @Transactional(readOnly = true)
    public TypingEvent typing(UUID me, UUID conversationId, boolean isTyping) {
        ChatConversation conversation = requireConversationOf(me, conversationId);
        return new TypingEvent(conversationId, me, isTyping, conversation.otherUser(me));
    }

    // ------------------------------------------------------------------ helpers

    private ChatConversation getOrCreateConversation(UUID me, UUID otherUserId) {
        if (otherUserId == null) {
            throw ChatException.badRequest("Thiếu người nhận");
        }
        if (otherUserId.equals(me)) {
            throw ChatException.badRequest("Không thể tự nhắn tin cho chính mình");
        }
        requireActiveUser(otherUserId);

        UserPair pair = UserPair.of(me, otherUserId);
        return conversations.findByUserAIdAndUserBId(pair.userA(), pair.userB())
                .orElseGet(() -> {
                    conversations.insertIfAbsent(UUID.randomUUID(), pair.userA(), pair.userB());
                    ChatConversation created = conversations.findByUserAIdAndUserBId(pair.userA(), pair.userB())
                            .orElseThrow(() -> new IllegalStateException("Không tạo được hội thoại"));
                    participants.insertIfAbsent(created.getId(), pair.userA());
                    participants.insertIfAbsent(created.getId(), pair.userB());
                    return created;
                });
    }

    /** Hoi thoai ton tai VA me la thanh vien. Khong phai thanh vien thi bao "khong tim thay" de khong lo thong tin. */
    private ChatConversation requireConversationOf(UUID me, UUID conversationId) {
        if (conversationId == null) {
            throw ChatException.badRequest("Thiếu conversationId");
        }
        return conversations.findById(conversationId)
                .filter(c -> c.hasParticipant(me))
                .orElseThrow(() -> ChatException.notFound("Không tìm thấy hội thoại"));
    }

    private ChatParticipant requireParticipant(UUID conversationId, UUID userId) {
        return participants.findById(new ChatParticipantId(conversationId, userId))
                .orElseThrow(() -> ChatException.notFound("Không tìm thấy hội thoại"));
    }

    private void requireActiveUser(UUID userId) {
        ChatUserInfo info = users.findById(userId)
                .orElseThrow(() -> ChatException.notFound("Người dùng không tồn tại"));
        if (!info.active()) {
            throw ChatException.forbidden("Tài khoản này đã bị khoá, không thể nhắn tin");
        }
    }

    private static String validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw ChatException.badRequest("Tin nhắn không được để trống");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw ChatException.badRequest("Tin nhắn tối đa " + MAX_CONTENT_LENGTH + " ký tự");
        }
        return content;
    }

    /** Thu tu giong cau SQL: (created_at, id). */
    private static boolean isNewer(ChatMessage a, ChatMessage b) {
        int byTime = a.getCreatedAt().compareTo(b.getCreatedAt());
        return byTime != 0 ? byTime > 0 : UserPair.compareLikePostgres(a.getId(), b.getId()) > 0;
    }

    /** Thoat ky tu dac biet cua LIKE de nguoi dung go "50%" thi tim dung chuoi "50%". */
    static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private List<ConversationDto> toConversationDtos(UUID me, List<ChatConversation> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        List<UUID> otherIds = list.stream().map(c -> c.otherUser(me)).distinct().toList();
        Map<UUID, ChatUserInfo> userInfo = users.findByIds(otherIds);

        List<UUID> lastIds = list.stream().map(ChatConversation::getLastMessageId).filter(Objects::nonNull).toList();
        Map<UUID, ChatMessage> lastMessages = messages.findAllById(lastIds).stream()
                .collect(Collectors.toMap(ChatMessage::getId, Function.identity()));

        Map<UUID, Long> unread = new HashMap<>();
        for (Object[] row : participants.countUnreadByConversation(me)) {
            unread.put((UUID) row[0], ((Number) row[1]).longValue());
        }

        List<ChatParticipantId> otherParticipantIds = list.stream()
                .map(c -> new ChatParticipantId(c.getId(), c.otherUser(me)))
                .toList();
        Map<UUID, UUID> otherLastRead = new HashMap<>();
        for (ChatParticipant p : participants.findAllById(otherParticipantIds)) {
            if (p.getLastReadMessageId() != null) {
                otherLastRead.put(p.getConversationId(), p.getLastReadMessageId());
            }
        }

        return list.stream().map(c -> {
            UUID other = c.otherUser(me);
            ChatUserInfo info = userInfo.get(other);
            ChatMessage last = c.getLastMessageId() == null ? null : lastMessages.get(c.getLastMessageId());
            return new ConversationDto(
                    c.getId(),
                    info == null ? ChatUserDto.unknown(other) : ChatUserDto.from(info),
                    last == null ? null : MessageDto.from(last, c.otherUser(last.getSenderId()), null),
                    c.getLastMessageAt(),
                    unread.getOrDefault(c.getId(), 0L),
                    otherLastRead.get(c.getId()));
        }).toList();
    }
}
