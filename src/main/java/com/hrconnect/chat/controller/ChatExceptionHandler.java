package com.hrconnect.chat.controller;

import com.hrconnect.chat.service.ChatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Chi xu ly loi cua controller trong package Chat (basePackages), khong anh huong module khac.
 * Tra ve dang ProblemDetail (RFC 9457). Neu nhom thong nhat format loi khac, chi can sua file nay.
 */
@RestControllerAdvice(basePackages = "com.hrconnect.chat")
public class ChatExceptionHandler {

    @ExceptionHandler(ChatException.class)
    public ProblemDetail handleChat(ChatException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.valueOf(e.getCode().httpStatus()), e.getMessage());
        problem.setProperty("code", e.getCode().name());
        return problem;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail handleBadInput(Exception e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Dữ liệu gửi lên không hợp lệ");
        problem.setProperty("code", "BAD_REQUEST");
        return problem;
    }
}
