package com.system.inventorysystem.observer.listener;

import com.system.inventorysystem.entity.ImportOrder;
import com.system.inventorysystem.enums.EventType;
import com.system.inventorysystem.observer.OrderEvent;
import com.system.inventorysystem.observer.OrderEventListener;
import com.system.inventorysystem.repository.ImportOrderRepository;
import com.system.inventorysystem.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(2)
// SupplierDebtListener sẽ cập nhật số dư nợ nếu đó là phiếu nhập.
public class SupplierDebtListener implements OrderEventListener {

    private final ImportOrderRepository importOrderRepository;
    private final SupplierService supplierService;

    @Override
    public boolean supports(OrderEvent event) {
        return event.getEventType() == EventType.IMPORT_COMPLETED;
    }

    @Override
    public void onOrderEvent(OrderEvent event) {
        if (!supports(event))
            return;

        ImportOrder order = importOrderRepository
                .findByIdWithDetails(event.getOrderId())
                .orElseThrow();

        if (order.getSupplier() == null)
            return;

        supplierService.updateDebt(order.getSupplier(), order.getFinalAmount());

        log.info("[SupplierDebtListener] Updated debt for supplier {} by {}",
                order.getSupplier().getCode(), order.getFinalAmount());
    }
}