package com.system.inventorysystem.observer;

import com.system.inventorysystem.enums.EventType;
import lombok.*;
import java.util.HashMap;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class OrderEvent {
    private final Long orderId;
    private final EventType eventType;
    private final String orderType;          // "IMPORT" hoặc "EXPORT"
    @Builder.Default
    private final Map<String, Object> payload = new HashMap<>();
}