package com.hrconnect.chat.push;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cau hinh push (chat.push.*).
 *
 * @param credentialsPath duong dan file JSON service account cua Firebase (bi mat, KHONG de trong repo).
 *                        De trong thi server khong gui push, cac chuc nang chat khac van chay binh thuong.
 * @param web             cau hinh Firebase phia trinh duyet (khong bi mat), tra cho client qua API.
 */
@ConfigurationProperties("chat.push")
public record ChatPushProperties(String credentialsPath, Web web) {

    public record Web(String apiKey,
                      String authDomain,
                      String projectId,
                      String storageBucket,
                      String messagingSenderId,
                      String appId,
                      String vapidKey) {
    }

    public boolean serverEnabled() {
        return credentialsPath != null && !credentialsPath.isBlank();
    }

    public boolean webEnabled() {
        return web != null && notBlank(web.apiKey()) && notBlank(web.appId()) && notBlank(web.vapidKey());
    }

    private static boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
