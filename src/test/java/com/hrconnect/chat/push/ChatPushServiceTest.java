package com.hrconnect.chat.push;

import com.hrconnect.chat.domain.ChatParticipant;
import com.hrconnect.chat.domain.ChatParticipantId;
import com.hrconnect.chat.domain.ChatPushToken;
import com.hrconnect.chat.domain.MessageType;
import com.hrconnect.chat.domain.PushPlatform;
import com.hrconnect.chat.dto.MessageDto;
import com.hrconnect.chat.port.ChatUserInfo;
import com.hrconnect.chat.port.ChatUserLookup;
import com.hrconnect.chat.repository.ChatParticipantRepository;
import com.hrconnect.chat.repository.ChatPushTokenRepository;
import com.hrconnect.chat.service.ChatException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit test cho ChatPushService, khong can database va Firebase. Chay: ./mvnw test -Dtest=ChatPushServiceTest */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatPushServiceTest {

    static final UUID AN = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID BINH = UUID.fromString("22222222-2222-2222-2222-222222222222");
    static final UUID CONV = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @Mock ChatPushTokenRepository tokens;
    @Mock ChatParticipantRepository participants;
    @Mock ChatUserLookup users;
    @Mock ChatPushSender sender;

    ChatPushService service;

    @BeforeEach
    void setUp() {
        service = new ChatPushService(tokens, participants, users, sender);
        when(users.findById(AN)).thenReturn(Optional.of(new ChatUserInfo(AN, "Nguyễn Văn An", null, true)));
        when(participants.findById(new ChatParticipantId(CONV, BINH)))
                .thenReturn(Optional.of(new ChatParticipant(new ChatParticipantId(CONV, BINH))));
        when(sender.send(anyList(), any())).thenReturn(Set.of());
    }

    @Test
    void notifiesRecipientWithSenderNameAndPreview() {
        when(tokens.findByUserId(BINH)).thenReturn(List.of(new ChatPushToken("t1", BINH, PushPlatform.WEB)));

        service.notifyNewMessage(message("Chào  Bình,\nmai họp nhé"));

        ArgumentCaptor<PushNotification> captor = ArgumentCaptor.forClass(PushNotification.class);
        verify(sender).send(org.mockito.ArgumentMatchers.eq(List.of("t1")), captor.capture());
        PushNotification n = captor.getValue();
        assertThat(n.title()).isEqualTo("Nguyễn Văn An");
        assertThat(n.body()).isEqualTo("Chào Bình, mai họp nhé");
        assertThat(n.data()).containsEntry("conversationId", CONV.toString())
                .containsEntry("senderId", AN.toString())
                .containsEntry("recipientId", BINH.toString());
    }

    @Test
    void skipsWhenRecipientMutedConversation() {
        ChatParticipant muted = new ChatParticipant(new ChatParticipantId(CONV, BINH));
        ReflectionTestUtils.setField(muted, "muted", true);
        when(participants.findById(new ChatParticipantId(CONV, BINH))).thenReturn(Optional.of(muted));
        when(tokens.findByUserId(BINH)).thenReturn(List.of(new ChatPushToken("t1", BINH, PushPlatform.WEB)));

        service.notifyNewMessage(message("hi"));

        verify(sender, never()).send(anyList(), any());
    }

    @Test
    void skipsWhenRecipientHasNoToken() {
        when(tokens.findByUserId(BINH)).thenReturn(List.of());

        service.notifyNewMessage(message("hi"));

        verify(sender, never()).send(anyList(), any());
    }

    @Test
    void removesTokensFirebaseRejects() {
        when(tokens.findByUserId(BINH)).thenReturn(List.of(
                new ChatPushToken("ok", BINH, PushPlatform.WEB),
                new ChatPushToken("gone", BINH, PushPlatform.WEB)));
        when(sender.send(anyList(), any())).thenReturn(Set.of("gone"));

        service.notifyNewMessage(message("hi"));

        verify(tokens).deleteAllByTokens(Set.of("gone"));
    }

    @Test
    void senderFailureDoesNotPropagate() {
        when(tokens.findByUserId(BINH)).thenReturn(List.of(new ChatPushToken("t1", BINH, PushPlatform.WEB)));
        when(sender.send(anyList(), any())).thenThrow(new IllegalStateException("firebase down"));

        service.notifyNewMessage(message("hi"));
    }

    @Test
    void registerDefaultsToWebAndRejectsBlankToken() {
        service.register(BINH, " abc ", null);
        verify(tokens).upsert("abc", BINH, "WEB");

        assertThatThrownBy(() -> service.register(BINH, " ", PushPlatform.WEB)).isInstanceOf(ChatException.class);
    }

    @Test
    void previewTruncatesLongContent() {
        String preview = ChatPushService.preview("a".repeat(500));
        assertThat(preview).hasSize(ChatPushService.MAX_BODY_LENGTH).endsWith("…");
    }

    private static MessageDto message(String content) {
        return new MessageDto(UUID.randomUUID(), CONV, AN, BINH, content, MessageType.TEXT, false,
                Instant.parse("2026-10-06T03:00:00Z"), null);
    }
}
