package com.system.inventorysystem.observer.listener;

import com.system.inventorysystem.enums.EventType;
import com.system.inventorysystem.observer.OrderEvent;
import com.system.inventorysystem.observer.OrderEventListener;
import com.system.inventorysystem.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LowStockNotifyListener implements OrderEventListener {

    private final ProductRepository productRepository;

    @Override
    public boolean supports(OrderEvent event) {
        return event.getEventType() == EventType.EXPORT_COMPLETED;
    }

    @Override
    public void onOrderEvent(OrderEvent event) {
        if (!supports(event)) return;

        var lowStockProducts = productRepository.findLowStockProducts();
        if (!lowStockProducts.isEmpty()) {
            lowStockProducts.forEach(p ->
                log.warn("[LowStockNotify] Sản phẩm '{}' (code: {}) còn {} / min {}",
                    p.getName(), p.getCode(), p.getQuantity(), p.getMinQuantity())
            );
            // Sau này có thể inject EmailService, NotificationService v.v.
        }
    }
}