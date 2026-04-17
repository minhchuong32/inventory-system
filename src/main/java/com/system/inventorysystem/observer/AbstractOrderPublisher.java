package com.system.inventorysystem.observer;

import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class AbstractOrderPublisher implements OrderEventPublisher {

    // Danh sách subscribers (quan hệ Aggregation 1 - n)
    protected final List<OrderEventListener> listeners = new ArrayList<>();

    @Override
    public void subscribe(OrderEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void unsubscribe(OrderEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void notifyListeners(OrderEvent event) {
        log.info("[Publisher] >>> Bắt đầu phát sự kiện: {} cho Order ID: {} (Loại: {})",
                event.getEventType(), event.getOrderId(), event.getOrderType());

        long start = System.currentTimeMillis();

        listeners.forEach(listener -> {
            if (listener.supports(event)) {
                log.debug("[Publisher] Đang gọi Listener: {}", listener.getClass().getSimpleName());
                try {
                    listener.onOrderEvent(event);
                } catch (Exception ex) {
                    log.error("[Publisher] !!! Lỗi tại {}: {}",
                            listener.getClass().getSimpleName(), ex.getMessage());
                }
            }
        });

        long duration = System.currentTimeMillis() - start;
        log.info("[Publisher] <<< Kết thúc phát sự kiện {}. Tổng thời gian xử lý: {}ms",
                event.getEventType(), duration);
    }
}