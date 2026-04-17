package com.system.inventorysystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BellNotificationDto {
    private final Long id;
    private final String type;
    private final String title;
    private final String message;
    private final String code;
    private final String statusLabel;
    private final String icon;
    private final String badgeClass;
    private final Integer quantity;
    private final Integer minQuantity;
    private final LocalDateTime occurredAt;
    private final String occurredAtText;
    private final String href;
}