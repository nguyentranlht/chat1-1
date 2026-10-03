package com.hrconnect.chat.dto;

import java.util.List;
import java.util.UUID;

/**
 * Mot trang lich su tin nhan.
 * items: xep tu CU den MOI (de hien thi luon).
 * nextBefore: truyen vao ?before= de tai trang cu hon; null nghia la da het.
 */
public record MessagePage(List<MessageDto> items, UUID nextBefore) {
}
