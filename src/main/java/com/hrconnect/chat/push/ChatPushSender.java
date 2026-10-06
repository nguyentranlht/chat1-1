package com.hrconnect.chat.push;

import java.util.List;
import java.util.Set;

/** Gui thong bao toi cac FCM token. Tach interface de test va de chay duoc khi chua cau hinh Firebase. */
public interface ChatPushSender {

    /** @return cac token khong con hop le (da go app, het han...) de xoa khoi database. */
    Set<String> send(List<String> tokens, PushNotification notification);
}
