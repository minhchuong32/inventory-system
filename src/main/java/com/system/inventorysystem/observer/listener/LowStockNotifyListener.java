package com.system.inventorysystem.observer.listener;

import com.system.inventorysystem.enums.EventType;
import com.system.inventorysystem.observer.OrderEvent;
import com.system.inventorysystem.observer.OrderEventListener;
import com.system.inventorysystem.service.StockAlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Order(3)
public class LowStockNotifyListener implements OrderEventListener {

    private final StockAlertService stockAlertService;

    @Override
    public boolean supports(OrderEvent event) {
        return event.getEventType() == EventType.IMPORT_COMPLETED
                || event.getEventType() == EventType.EXPORT_COMPLETED
                || event.getEventType() == EventType.ORDER_CANCELLED;
    }

    @Override
    public void onOrderEvent(OrderEvent event) {
        stockAlertService.refreshLowStockAlerts();
    }
}