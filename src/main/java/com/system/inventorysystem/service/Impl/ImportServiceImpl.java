package com.system.inventorysystem.service.Impl;

import com.system.inventorysystem.entity.ImportDetail;
import com.system.inventorysystem.entity.ImportOrder;
import com.system.inventorysystem.entity.Product;
import com.system.inventorysystem.enums.MovementType;
import com.system.inventorysystem.enums.EventType;
import com.system.inventorysystem.observer.OrderEvent;
import com.system.inventorysystem.observer.OrderEventListener;
import com.system.inventorysystem.observer.OrderEventPublisher;
import com.system.inventorysystem.exception.ResourceNotFoundException;
import com.system.inventorysystem.repository.ImportOrderRepository;
import com.system.inventorysystem.service.ImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class ImportServiceImpl implements ImportService, OrderEventPublisher {


    private final ImportOrderRepository importOrderRepository;
    // private final ProductRepository productRepository;
    // private final StockService stockService;
    private final List<OrderEventListener> listeners;

    // Spring tự inject tất cả bean implement OrderEventListener
    public ImportServiceImpl(ImportOrderRepository importOrderRepository,
                             List<OrderEventListener> listeners) {
        this.importOrderRepository = importOrderRepository;
        this.listeners = listeners;
    }

    // ── OrderEventPublisher ────────────────────────────────────────────────
    @Override
    public void publish(OrderEvent event) {
        listeners.forEach(listener -> {
            try {
                listener.onOrderEvent(event);
            } catch (Exception ex) {
                log.error("[ImportService] Listener {} thất bại với event {}: {}",
                        listener.getClass().getSimpleName(),
                        event.getEventType(),
                        ex.getMessage(), ex);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ImportOrder> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return kw != null
            ? importOrderRepository.searchOrders(kw, pageable)
            : importOrderRepository.findAllOrdered(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ImportOrder> findById(Long id) {
        return importOrderRepository.findByIdWithDetails(id);
    }

    @Override
    public ImportOrder save(ImportOrder order) {
        if (order.getId() == null) {
            order.setCode(generateCode());
            order.setOrderDate(LocalDate.now());
        }
        // Link details to order and compute totals
        if (order.getDetails() != null) {
            order.getDetails().forEach(d -> {
                d.setImportOrder(order);
                d.calculateTotal();
            });
            order.calculateTotal();
        }
        ImportOrder saved = importOrderRepository.save(order);
        log.info("Saved import order: {}", saved.getCode());
        return saved;
    }

    @Override
    public ImportOrder complete(Long id) {
        ImportOrder order = findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("phiếu nhập", id));

        // order.complete(); // throws if already final

        // // Update inventory for each detail line
        // for (ImportDetail detail : order.getDetails()) {
        //     Product product = productRepository.findById(detail.getProduct().getId())
        //         .orElseThrow(() -> new ResourceNotFoundException("sản phẩm",
        //             detail.getProduct().getId()));

        //     int before = product.getQuantity() != null ? product.getQuantity() : 0;
        //     product.increaseStock(detail.getQuantity());
        //     int after = product.getQuantity();
        //     productRepository.save(product);

        //     stockService.recordMovement(
        //         product.getId(),
        //         product.getWarehouse() != null ? product.getWarehouse().getId() : null,
        //         MovementType.IN,
        //         detail.getQuantity(), before, after,
        //         order.getCode(), "IMPORT",
        //         "Nhập kho theo phiếu " + order.getCode()
        //     );
        // }
        // ImportOrder completed = importOrderRepository.save(order);
        // log.info("Completed import order: {}", completed.getCode());
        // return completed;


        // Chỉ đổi trạng thái — ném exception nếu đã ở trạng thái cuối
        order.complete();
        ImportOrder completed = importOrderRepository.save(order);
log.info(">>> [ImportService] Bắt đầu publish event IMPORT_COMPLETED cho phiếu {}", completed.getCode());

        // Phát sự kiện — các Observer tự xử lý phần việc của mình
        publish(OrderEvent.builder()
                .orderId(completed.getId())
                .eventType(EventType.IMPORT_COMPLETED)
                .orderType("IMPORT")
                .build());

log.info(">>> [ImportService] Đã publish xong, {} listeners đã xử lý", listeners.size());
        log.info("Completed import order: {}", completed.getCode());
        return completed;
    }

    @Override
    public void cancel(Long id) {
        ImportOrder order = findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("phiếu nhập", id));
        order.cancel();
        importOrderRepository.save(order);
        // log.info("Cancelled import order: {}", order.getCode());

        publish(OrderEvent.builder()
                .orderId(id)
                .eventType(EventType.ORDER_CANCELLED)
                .orderType("IMPORT")
                .build());

        log.info("Cancelled import order: {}", order.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public long countThisMonth() {
        LocalDate now = LocalDate.now();
        return importOrderRepository.countByYearAndMonth(now.getYear(), now.getMonthValue());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountThisMonth() {
        LocalDate now = LocalDate.now();
        BigDecimal result = importOrderRepository.sumAmountByYearAndMonth(
            now.getYear(), now.getMonthValue());
        return result != null ? result : BigDecimal.ZERO;
    }

    private String generateCode() {
        long seq = importOrderRepository.count() + 1;
        return String.format("PN%06d", seq);
    }
}
