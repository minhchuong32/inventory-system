package com.system.inventorysystem.service.Impl;

import com.system.inventorysystem.entity.ExportDetail;
import com.system.inventorysystem.entity.ExportOrder;
import com.system.inventorysystem.entity.Product;
import com.system.inventorysystem.enums.EventType;
import com.system.inventorysystem.enums.OrderStatus;
import com.system.inventorysystem.observer.AbstractOrderPublisher;
import com.system.inventorysystem.observer.OrderEvent;
import com.system.inventorysystem.observer.OrderEventListener;
import com.system.inventorysystem.exception.ResourceNotFoundException;
import com.system.inventorysystem.repository.ExportOrderRepository;
import com.system.inventorysystem.repository.ProductRepository;
import com.system.inventorysystem.service.ExportService;
import com.system.inventorysystem.service.StockAlertService;
import com.system.inventorysystem.strategy.discount.DiscountStrategy;
import com.system.inventorysystem.strategy.discount.DiscountStrategyFactory;
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
public class ExportServiceImpl extends AbstractOrderPublisher implements ExportService {

    private final ExportOrderRepository exportOrderRepository;
    private final ProductRepository productRepository;
    private final DiscountStrategyFactory discountStrategyFactory;
    private final StockAlertService stockAlertService;

    public ExportServiceImpl(ExportOrderRepository exportOrderRepository,
            ProductRepository productRepository,
            List<OrderEventListener> allListeners,
            DiscountStrategyFactory discountStrategyFactory,
            StockAlertService stockAlertService) {
        this.exportOrderRepository = exportOrderRepository;
        this.productRepository = productRepository;
        this.discountStrategyFactory = discountStrategyFactory;
        this.stockAlertService = stockAlertService;
        // Đăng ký toàn bộ listener vào danh sách quản lý của cha
        allListeners.forEach(this::subscribe);
    }

    @Transactional(readOnly = true)
    public Page<ExportOrder> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        return kw != null ? exportOrderRepository.searchOrders(kw, pageable)
                : exportOrderRepository.findAllOrdered(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExportOrder> findById(Long id) {
        return exportOrderRepository.findByIdWithDetails(id);
    }

    @Override
    public ExportOrder save(ExportOrder order) {
        if (order.getId() == null) {
            order.setCode(generateCode());
            order.setOrderDate(LocalDate.now());
        }
        if (order.getDetails() != null) {
            order.getDetails().forEach(d -> {
                d.setExportOrder(order);
                d.calculateTotal();
            });
            order.calculateTotal();
        }

        // ── Strategy Pattern: Áp dụng giảm giá theo loại khách hàng ────────
        if (order.getCustomer() != null) {
            String customerType = order.getCustomer().getCustomerType();
            DiscountStrategy strategy = discountStrategyFactory.getStrategy(customerType);
            order.setDiscountAmount(strategy.calculateDiscount(order.getTotalAmount()));
            order.recalculateFinalAmount(); // cập nhật finalAmount sau khi có discount
        }
        // ────────────────────────────────────────────────────────────────────

        ExportOrder saved = exportOrderRepository.save(order);
        stockAlertService.refreshLowStockAlerts();
        return saved;
    }

    @Override
    public ExportOrder complete(Long id) {
        ExportOrder order = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("phiếu xuất", id));

        // Validate stock before committing
        for (ExportDetail detail : order.getDetails()) {
            Product p = productRepository.findById(detail.getProduct().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("sản phẩm", detail.getProduct().getId()));
            if (!p.canExport(detail.getQuantity())) {
                throw new com.system.inventorysystem.exception.InsufficientStockException(
                        p.getName(), detail.getQuantity(),
                        p.getQuantity() != null ? p.getQuantity() : 0);
            }
        }

        // order.complete();

        // for (ExportDetail detail : order.getDetails()) {
        // Product product =
        // productRepository.findById(detail.getProduct().getId()).get();
        // int before = product.getQuantity();
        // product.decreaseStock(detail.getQuantity());
        // int after = product.getQuantity();
        // productRepository.save(product);

        // stockService.recordMovement(product.getId(),
        // product.getWarehouse() != null ? product.getWarehouse().getId() : null,
        // MovementType.OUT, detail.getQuantity(), before, after,
        // order.getCode(), "EXPORT", "Xuất kho theo phiếu " + order.getCode());
        // }
        // return exportOrderRepository.save(order);

        order.complete();
        ExportOrder completed = exportOrderRepository.save(order);

        // Gọi notify theo cấu trúc mới
        notifyListeners(OrderEvent.builder()
                .orderId(completed.getId())
                .eventType(EventType.EXPORT_COMPLETED)
                .orderType("EXPORT")
                .build());

        return completed;
    }

    @Override
    public void cancel(Long id) {
        ExportOrder order = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("phiếu xuất", id));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Đơn hàng đã được hủy trước đó.");
        }
        order.cancel();
        exportOrderRepository.save(order);

        notifyListeners(OrderEvent.builder()
                .orderId(id)
                .eventType(EventType.ORDER_CANCELLED)
                .orderType("EXPORT")
                .build());

        log.info("Cancelled export order: {}", order.getCode());
    }

    @Override
    @Transactional(readOnly = true)
    public long countThisMonth() {
        LocalDate now = LocalDate.now();
        return exportOrderRepository.countByYearAndMonth(now.getYear(), now.getMonthValue());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountThisMonth() {
        LocalDate now = LocalDate.now();
        BigDecimal r = exportOrderRepository.sumAmountByYearAndMonth(now.getYear(), now.getMonthValue());
        return r != null ? r : BigDecimal.ZERO;
    }

    private String generateCode() {
        return String.format("PX%06d", exportOrderRepository.count() + 1);
    }
}
