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
// StockUpdateListener sẽ tự đi tìm dữ liệu đơn hàng để tăng/giảm kho và ghi log
// biến động.
public class StockUpdateListener implements OrderEventListener {

    private final ProductRepository productRepository;
    private final ImportOrderRepository importOrderRepository;
    private final ExportOrderRepository exportOrderRepository;
    private final StockService stockService;

    @Override
    public boolean supports(OrderEvent event) {
        return event.getEventType() == EventType.IMPORT_COMPLETED
                || event.getEventType() == EventType.EXPORT_COMPLETED;
    }

    @Override
    public void onOrderEvent(OrderEvent event) {
        if (!supports(event))
            return;

        log.info("[StockUpdateListener] Handling event: {}", event.getEventType());

        if (event.getEventType() == EventType.IMPORT_COMPLETED) {
            handleImportCompleted(event);
        } else {
            handleExportCompleted(event);
        }
    }

    private void handleImportCompleted(OrderEvent event) {
        ImportOrder order = importOrderRepository
                .findByIdWithDetails(event.getOrderId())
                .orElseThrow();

        for (ImportDetail detail : order.getDetails()) {
            Product product = productRepository.findById(detail.getProduct().getId()).orElseThrow();
            int before = product.getQuantity();
            product.increaseStock(detail.getQuantity());
            productRepository.save(product);

            stockService.recordMovement(
                    product.getId(),
                    product.getWarehouse() != null ? product.getWarehouse().getId() : null,
                    MovementType.IN, detail.getQuantity(), before, product.getQuantity(),
                    order.getCode(), "IMPORT", "Nhập kho theo phiếu " + order.getCode());
        }
    }

    private void handleExportCompleted(OrderEvent event) {
        ExportOrder order = exportOrderRepository
                .findByIdWithDetails(event.getOrderId())
                .orElseThrow();

        for (ExportDetail detail : order.getDetails()) {
            Product product = productRepository.findById(detail.getProduct().getId()).orElseThrow();
            int before = product.getQuantity();
            product.decreaseStock(detail.getQuantity());
            productRepository.save(product);

            stockService.recordMovement(
                    product.getId(),
                    product.getWarehouse() != null ? product.getWarehouse().getId() : null,
                    MovementType.OUT, detail.getQuantity(), before, product.getQuantity(),
                    order.getCode(), "EXPORT", "Xuất kho theo phiếu " + order.getCode());
        }
    }
}