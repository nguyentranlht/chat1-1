package com.hrconnect.chat.service;

import com.hrconnect.chat.domain.ChatConversation;
import com.hrconnect.chat.domain.ChatMessage;
import com.hrconnect.chat.domain.ChatParticipant;
import com.hrconnect.chat.domain.ChatParticipantId;
import com.hrconnect.chat.domain.MessageType;
import com.hrconnect.chat.domain.UserPair;
import com.hrconnect.chat.dto.MessageDto;
import com.hrconnect.chat.dto.MessagePage;
import com.hrconnect.chat.dto.SendMessageRequest;
import com.hrconnect.chat.port.ChatUserInfo;
import com.hrconnect.chat.port.ChatUserLookup;
import com.hrconnect.chat.repository.ChatConversationRepository;
import com.hrconnect.chat.repository.ChatMessageRepository;
import com.hrconnect.chat.repository.ChatParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit test cho ChatService, khong can database. Chay: ./mvnw test -Dtest=ChatServiceTest */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatServiceTest {

    static final UUID AN = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID BINH = UUID.fromString("22222222-2222-2222-2222-222222222222");
    static final UUID CUONG = UUID.fromString("33333333-3333-3333-3333-333333333333");
    static final UUID LOCKED = UUID.fromString("99999999-9999-9999-9999-999999999999");
    static final Instant NOW = Instant.parse("2026-10-03T03:00:00Z");

    @Mock ChatConversationRepository conversations;
    @Mock ChatParticipantRepository participants;
    @Mock ChatMessageRepository messages;
    @Mock ChatUserLookup users;

    ChatService service;
    ChatConversation anBinh;

    @BeforeEach
    void setUp() {
        service = new ChatService(conversations, participants, messages, users, Clock.fixed(NOW, ZoneOffset.UTC));
        when(users.findById(AN)).thenReturn(Optional.of(new ChatUserInfo(AN, "An", null, true)));
        when(users.findById(BINH)).thenReturn(Optional.of(new ChatUserInfo(BINH, "Bình", null, true)));
        when(users.findById(CUONG)).thenReturn(Optional.of(new ChatUserInfo(CUONG, "Cường", null, true)));
        when(users.findById(LOCKED)).thenReturn(Optional.of(new ChatUserInfo(LOCKED, "Khoá", null, false)));

        anBinh = new ChatConversation(UUID.randomUUID(), UserPair.of(AN, BINH));
        when(conversations.findById(anBinh.getId())).thenReturn(Optional.of(anBinh));
        when(messages.save(any(ChatMessage.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    // ---------------------------------------------------------------- UserPair

    @Test
    void userPair_sapXepGiongPostgres_voiUuidCoBitCao() {
        UUID high = UUID.fromString("f0000000-0000-0000-0000-000000000000");
        // UUID.compareTo cua Java coi f000... la so AM nen xep truoc 1111...; PostgreSQL thi nguoc lai
        assertThat(high.compareTo(AN)).isNegative();
        UserPair pair = UserPair.of(high, AN);
        assertThat(pair.userA()).isEqualTo(AN);
        assertThat(pair.userB()).isEqualTo(high);
        assertThat(UserPair.of(AN, high)).isEqualTo(pair);
    }

    // ---------------------------------------------------------------- CHT-01

    @Test
    void openConversation_chuaCo_thiTaoMoiVoiCapDaSapXep() {
        ChatConversation created = new ChatConversation(UUID.randomUUID(), UserPair.of(AN, BINH));
        when(conversations.findByUserAIdAndUserBId(AN, BINH))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(created));

        // Binh mo voi An: van phai luu theo thu tu (AN, BINH)
        var dto = service.openConversation(BINH, AN);

        assertThat(dto.id()).isEqualTo(created.getId());
        verify(conversations).insertIfAbsent(any(UUID.class), eq(AN), eq(BINH));
        verify(participants).insertIfAbsent(created.getId(), AN);
        verify(participants).insertIfAbsent(created.getId(), BINH);
    }

    @Test
    void openConversation_daCo_thiKhongTaoThem() {
        when(conversations.findByUserAIdAndUserBId(AN, BINH)).thenReturn(Optional.of(anBinh));

        var dto = service.openConversation(AN, BINH);

        assertThat(dto.id()).isEqualTo(anBinh.getId());
        assertThat(dto.otherUser().id()).isEqualTo(BINH);
        verify(conversations, never()).insertIfAbsent(any(), any(), any());
    }

    @Test
    void openConversation_voiChinhMinh_biTuChoi() {
        assertThatThrownBy(() -> service.openConversation(AN, AN))
                .isInstanceOf(ChatException.class)
                .extracting(e -> ((ChatException) e).getCode()).isEqualTo(ChatErrorCode.BAD_REQUEST);
    }

    @Test
    void openConversation_voiTaiKhoanBiKhoa_biTuChoi() {
        assertThatThrownBy(() -> service.openConversation(AN, LOCKED))
                .isInstanceOf(ChatException.class)
                .extracting(e -> ((ChatException) e).getCode()).isEqualTo(ChatErrorCode.FORBIDDEN);
    }

    // ---------------------------------------------------------------- CHT-03

    @Test
    void sendMessage_luuTin_capNhatTinCuoi_vaNguoiGuiCoiNhuDaDoc() {
        ChatParticipant binhState = new ChatParticipant(new ChatParticipantId(anBinh.getId(), BINH));
        when(participants.findById(new ChatParticipantId(anBinh.getId(), BINH))).thenReturn(Optional.of(binhState));

        MessageDto dto = service.sendMessage(BINH, new SendMessageRequest(anBinh.getId(), null, "Chào An", null, "tmp-1"));

        assertThat(dto.senderId()).isEqualTo(BINH);
        assertThat(dto.recipientId()).isEqualTo(AN);
        assertThat(dto.content()).isEqualTo("Chào An");
        assertThat(dto.type()).isEqualTo(MessageType.TEXT);
        assertThat(dto.clientMsgId()).isEqualTo("tmp-1");
        assertThat(anBinh.getLastMessageId()).isEqualTo(dto.id());
        assertThat(binhState.getLastReadMessageId()).isEqualTo(dto.id());
    }

    @Test
    void sendMessage_nguoiNgoaiHoiThoai_khongGuiDuoc() {
        assertThatThrownBy(() -> service.sendMessage(CUONG, new SendMessageRequest(anBinh.getId(), null, "xin vào", null, null)))
                .isInstanceOf(ChatException.class)
                .extracting(e -> ((ChatException) e).getCode()).isEqualTo(ChatErrorCode.NOT_FOUND);
        verify(messages, never()).save(any());
    }

    @Test
    void sendMessage_tinRongHoacQuaDai_biTuChoi() {
        assertThatThrownBy(() -> service.sendMessage(AN, new SendMessageRequest(anBinh.getId(), null, "   ", null, null)))
                .isInstanceOf(ChatException.class);
        assertThatThrownBy(() -> service.sendMessage(AN, new SendMessageRequest(anBinh.getId(), null, "x".repeat(4001), null, null)))
                .isInstanceOf(ChatException.class);
    }

    // ---------------------------------------------------------------- CHT-05

    @Test
    void getHistory_traVeCuDenMoi_vaCoNextBeforeKhiConTin() {
        List<ChatMessage> newestFirst = new ArrayList<>();
        for (int i = 5; i >= 1; i--) {
            newestFirst.add(new ChatMessage(anBinh.getId(), AN, "tin " + i, MessageType.TEXT, NOW.plusSeconds(i)));
        }
        // limit = 4 -> service xin 5 dong de biet con trang sau hay khong
        when(messages.findLatest(anBinh.getId(), 5)).thenReturn(newestFirst);

        MessagePage page = service.getHistory(AN, anBinh.getId(), null, 4);

        assertThat(page.items()).extracting(MessageDto::content).containsExactly("tin 2", "tin 3", "tin 4", "tin 5");
        assertThat(page.nextBefore()).isEqualTo(newestFirst.get(3).getId());
    }

    @Test
    void getHistory_trangCuoi_nextBeforeLaNull() {
        when(messages.findLatest(eq(anBinh.getId()), anyInt()))
                .thenReturn(List.of(new ChatMessage(anBinh.getId(), AN, "duy nhất", MessageType.TEXT, NOW)));

        MessagePage page = service.getHistory(AN, anBinh.getId(), null, null);

        assertThat(page.items()).hasSize(1);
        assertThat(page.nextBefore()).isNull();
    }

    // ---------------------------------------------------------------- CHT-06

    @Test
    void recall_trong24Gio_thanhCong_vaAnNoiDung() {
        ChatMessage m = new ChatMessage(anBinh.getId(), AN, "lỡ gửi", MessageType.TEXT, NOW.minus(Duration.ofHours(1)));
        when(messages.findById(m.getId())).thenReturn(Optional.of(m));

        MessageDto dto = service.recall(AN, m.getId());

        assertThat(dto.recalled()).isTrue();
        assertThat(dto.content()).isNull();
    }

    @Test
    void recall_qua24Gio_biTuChoi() {
        ChatMessage m = new ChatMessage(anBinh.getId(), AN, "cũ", MessageType.TEXT, NOW.minus(Duration.ofHours(25)));
        when(messages.findById(m.getId())).thenReturn(Optional.of(m));

        assertThatThrownBy(() -> service.recall(AN, m.getId()))
                .isInstanceOf(ChatException.class)
                .extracting(e -> ((ChatException) e).getCode()).isEqualTo(ChatErrorCode.BAD_REQUEST);
    }

    @Test
    void recall_tinCuaNguoiKhac_biTuChoi() {
        ChatMessage m = new ChatMessage(anBinh.getId(), BINH, "của Bình", MessageType.TEXT, NOW);
        when(messages.findById(m.getId())).thenReturn(Optional.of(m));

        assertThatThrownBy(() -> service.recall(AN, m.getId()))
                .isInstanceOf(ChatException.class)
                .extracting(e -> ((ChatException) e).getCode()).isEqualTo(ChatErrorCode.FORBIDDEN);
    }

    // ---------------------------------------------------------------- tim kiem

    @Test
    void escapeLike_thoatKyTuDacBiet() {
        assertThat(ChatService.escapeLike("50%_a\\b")).isEqualTo("50\\%\\_a\\\\b");
    }
}
