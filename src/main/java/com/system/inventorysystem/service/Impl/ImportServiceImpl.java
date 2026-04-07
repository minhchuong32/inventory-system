package com.system.inventorysystem.service.Impl;

import com.system.inventorysystem.entity.ImportOrder;
import com.system.inventorysystem.enums.OrderStatus;
import com.system.inventorysystem.enums.EventType;
import com.system.inventorysystem.observer.AbstractOrderPublisher;
import com.system.inventorysystem.observer.OrderEvent;
import com.system.inventorysystem.observer.OrderEventListener;
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
public class ImportServiceImpl extends AbstractOrderPublisher implements ImportService {

    private final ImportOrderRepository importOrderRepository;

    // Tận dụng cơ chế Inject List của Spring để nạp tất cả Listener vào Publisher
    public ImportServiceImpl(ImportOrderRepository importOrderRepository,
            List<OrderEventListener> allListeners) {
        this.importOrderRepository = importOrderRepository;
        allListeners.forEach(this::subscribe);
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
        // Chỉ đổi trạng thái — ném exception nếu đã ở trạng thái cuối
        order.complete();
        ImportOrder completed = importOrderRepository.save(order);

        // Sử dụng phương thức từ lớp cha
        notifyListeners(OrderEvent.builder()
                .orderId(completed.getId())
                .eventType(EventType.IMPORT_COMPLETED)
                .orderType("IMPORT")
                .build());

        return completed;
    }

    @Override
    public void cancel(Long id) {
        ImportOrder order = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("phiếu nhập", id));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Đơn hàng đã được hủy trước đó.");
        }
        order.cancel();
        importOrderRepository.save(order);
        // log.info("Cancelled import order: {}", order.getCode());

        notifyListeners(OrderEvent.builder()
                .orderId(id)
                .eventType(EventType.ORDER_CANCELLED)
                .orderType("IMPORT")
                .build());

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
