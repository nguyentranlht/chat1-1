package com.hrconnect.chat.push;

import java.util.Map;

/** Mot thong bao can day. data: cac truong bo sung de client mo dung hoi thoai khi bam vao. */
public record PushNotification(String title, String body, String collapseKey, Map<String, String> data) {
}
