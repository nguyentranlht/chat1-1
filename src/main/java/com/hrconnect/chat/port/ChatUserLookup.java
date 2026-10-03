package com.hrconnect.chat.port;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Chat can biet thong tin nguoi dung (ten, avatar, con hoat dong khong).
 *
 * <p>Hien tai: ban tam {@code devstub.DevChatUserLookup} (profile chat-dev).
 * Khi ghep voi Core: viet class moi implement interface nay, goi UserService cua Core.
 */
public interface ChatUserLookup {

    Optional<ChatUserInfo> findById(UUID userId);

    /** Lay nhieu user mot luc (dung cho danh sach hoi thoai). User khong ton tai thi khong co trong map. */
    Map<UUID, ChatUserInfo> findByIds(Collection<UUID> userIds);
}
