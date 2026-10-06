package com.hrconnect.chat.push;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.SendResponse;
import com.google.firebase.messaging.WebpushConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gui qua Firebase Cloud Messaging.
 *
 * <p>Web: gui dang data-only, service worker (firebase-messaging-sw.js) tu hien thong bao de dieu khien
 * duoc noi dung va hanh vi khi bam. Android/iOS: kem notification de he dieu hanh tu hien khi app dang tat.
 */
public class FirebaseChatPushSender implements ChatPushSender {

    private static final Logger log = LoggerFactory.getLogger(FirebaseChatPushSender.class);

    /** Gioi han cua FCM cho mot lan gui multicast. */
    private static final int MAX_TOKENS_PER_REQUEST = 500;

    private final FirebaseMessaging messaging;

    public FirebaseChatPushSender(FirebaseMessaging messaging) {
        this.messaging = messaging;
    }

    @Override
    public Set<String> send(List<String> tokens, PushNotification n) {
        Set<String> invalid = new HashSet<>();
        for (int from = 0; from < tokens.size(); from += MAX_TOKENS_PER_REQUEST) {
            List<String> batch = tokens.subList(from, Math.min(from + MAX_TOKENS_PER_REQUEST, tokens.size()));
            try {
                BatchResponse response = messaging.sendEachForMulticast(build(batch, n));
                List<SendResponse> results = response.getResponses();
                for (int i = 0; i < results.size(); i++) {
                    SendResponse r = results.get(i);
                    if (!r.isSuccessful() && isTokenGone(r.getException())) {
                        invalid.add(batch.get(i));
                    } else if (!r.isSuccessful()) {
                        log.warn("Gửi push thất bại: {}", r.getException().getMessage());
                    }
                }
            } catch (FirebaseMessagingException e) {
                log.warn("Gửi push thất bại: {}", e.getMessage());
            }
        }
        return invalid;
    }

    private static MulticastMessage build(List<String> tokens, PushNotification n) {
        Map<String, String> data = new HashMap<>(n.data());
        data.put("title", n.title());
        data.put("body", n.body());
        return MulticastMessage.builder()
                .addAllTokens(tokens)
                .putAllData(data)
                .setWebpushConfig(WebpushConfig.builder()
                        .putHeader("Urgency", "high")
                        .putHeader("Topic", n.collapseKey().replace("-", ""))
                        .build())
                .setAndroidConfig(AndroidConfig.builder()
                        .setPriority(AndroidConfig.Priority.HIGH)
                        .setCollapseKey(n.collapseKey())
                        .setNotification(AndroidNotification.builder()
                                .setTitle(n.title())
                                .setBody(n.body())
                                .setTag(n.collapseKey())
                                .build())
                        .build())
                .setApnsConfig(ApnsConfig.builder()
                        .setAps(Aps.builder()
                                .setAlert(ApsAlert.builder().setTitle(n.title()).setBody(n.body()).build())
                                .setSound("default")
                                .setThreadId(n.collapseKey())
                                .build())
                        .build())
                .build();
    }

    private static boolean isTokenGone(FirebaseMessagingException e) {
        return e != null && (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT);
    }
}
