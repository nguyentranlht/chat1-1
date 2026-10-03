package com.hrconnect.chat.service;

public enum ChatErrorCode {
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    BAD_REQUEST(400),
    CONFLICT(409);

    private final int httpStatus;

    ChatErrorCode(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public int httpStatus() {
        return httpStatus;
    }
}
