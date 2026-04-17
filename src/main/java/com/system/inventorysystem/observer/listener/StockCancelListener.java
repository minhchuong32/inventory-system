package com.system.inventorysystem.observer.listener;

import com.system.inventorysystem.entity.*;
import com.system.inventorysystem.enums.EventType;
import com.system.inventorysystem.enums.MovementType;
import com.system.inventorysystem.observer.OrderEvent;
import com.system.inventorysystem.observer.OrderEventListener;
import com.system.inventorysystem.repository.*;
import com.system.inventorysystem.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class StockCancelListener implements OrderEventListener {

    private final ProductRepository productRepository;
    private final ImportOrderRepository importOrderRepository;
    private final ExportOrderRepository exportOrderRepository;
    private final StockService stockService;

    @Override
    public boolean supports(OrderEvent event) {
        // Chỉ xử lý khi sự kiện là HỦY ĐƠN HÀNG
        return event.getEventType() == EventType.ORDER_CANCELLED;
    }

    @Override
    public void onOrderEvent(OrderEvent event) {
        if (!supports(event))
            return;

        log.info("[StockCancelListener] Đang hoàn kho cho đơn {} loại {}", event.getOrderId(), event.getOrderType());

        if ("IMPORT".equals(event.getOrderType())) {
            handleCancelImport(event.getOrderId());
        } else if ("EXPORT".equals(event.getOrderType())) {
            handleCancelExport(event.getOrderId());
        }
    }

    private void handleCancelImport(Long orderId) {
        ImportOrder order = importOrderRepository.findByIdWithDetails(orderId).orElseThrow();

        for (ImportDetail detail : order.getDetails()) {
            Product product = productRepository.findById(detail.getProduct().getId()).orElseThrow();
            int before = product.getQuantity();

            // Hủy nhập -> Trừ lại kho
            product.setQuantity(before - detail.getQuantity());
            productRepository.save(product);

            stockService.recordMovement(
                    product.getId(),
                    product.getWarehouse() != null ? product.getWarehouse().getId() : null,
                    MovementType.OUT, detail.getQuantity(), before, product.getQuantity(),
                    order.getCode(), "CANCEL_IMPORT", "Hoàn kho do hủy phiếu nhập " + order.getCode());
        }
    }

    private void handleCancelExport(Long orderId) {
        ExportOrder order = exportOrderRepository.findByIdWithDetails(orderId).orElseThrow();

        for (ExportDetail detail : order.getDetails()) {
            Product product = productRepository.findById(detail.getProduct().getId()).orElseThrow();
            int before = product.getQuantity();

            // Hủy xuất -> Cộng lại kho
            product.setQuantity(before + detail.getQuantity());
            productRepository.save(product);

            stockService.recordMovement(
                    product.getId(),
                    product.getWarehouse() != null ? product.getWarehouse().getId() : null,
                    MovementType.IN, detail.getQuantity(), before, product.getQuantity(),
                    order.getCode(), "CANCEL_EXPORT", "Hoàn kho do hủy phiếu xuất " + order.getCode());
        }
    }
}