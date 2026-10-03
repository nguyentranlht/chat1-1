package com.hrconnect.chat.dto;

/** Loi gui ve client qua WebSocket (/user/queue/errors). */
public record ChatErrorDto(String code, String message) {
}
