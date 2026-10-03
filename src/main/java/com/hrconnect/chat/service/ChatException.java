package com.hrconnect.chat.service;

/** Loi nghiep vu cua Chat. Duoc chuyen thanh HTTP status (REST) hoac gui ve /user/queue/errors (WebSocket). */
public class ChatException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ChatErrorCode code;

    public ChatException(ChatErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ChatErrorCode getCode() {
        return code;
    }

    public static ChatException notFound(String message) {
        return new ChatException(ChatErrorCode.NOT_FOUND, message);
    }

    public static ChatException forbidden(String message) {
        return new ChatException(ChatErrorCode.FORBIDDEN, message);
    }

    public static ChatException badRequest(String message) {
        return new ChatException(ChatErrorCode.BAD_REQUEST, message);
    }

    public static ChatException unauthorized(String message) {
        return new ChatException(ChatErrorCode.UNAUTHORIZED, message);
    }
}
