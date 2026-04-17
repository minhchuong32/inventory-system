package com.system.inventorysystem.service;

import com.system.inventorysystem.dto.BellNotificationDto;
import com.system.inventorysystem.entity.ExportOrder;
import com.system.inventorysystem.entity.ImportOrder;
import com.system.inventorysystem.entity.Product;
import com.system.inventorysystem.entity.base.AbstractOrder;
import com.system.inventorysystem.enums.OrderStatus;
import com.system.inventorysystem.repository.ExportOrderRepository;
import com.system.inventorysystem.repository.ImportOrderRepository;
import com.system.inventorysystem.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockAlertService {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter NOTIFICATION_TIME_FORMATTER = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm");

    private final ProductRepository productRepository;
    private final ImportOrderRepository importOrderRepository;
    private final ExportOrderRepository exportOrderRepository;

    private volatile List<BellNotificationDto> notifications = List.of();

    @PostConstruct
    public void init() {
        refreshLowStockAlerts();
    }

    public synchronized void refreshLowStockAlerts() {
        List<BellNotificationDto> items = new ArrayList<>();

        items.addAll(buildOrderNotifications(importOrderRepository.findByStatusAndDeletedFalse(OrderStatus.PENDING),
                "IMPORT"));
        items.addAll(buildOrderNotifications(importOrderRepository.findByStatusAndDeletedFalse(OrderStatus.COMPLETED),
                "IMPORT"));
        items.addAll(buildOrderNotifications(importOrderRepository.findByStatusAndDeletedFalse(OrderStatus.CANCELLED),
                "IMPORT"));

        items.addAll(buildOrderNotifications(exportOrderRepository.findByStatusAndDeletedFalse(OrderStatus.PENDING),
                "EXPORT"));
        items.addAll(buildOrderNotifications(exportOrderRepository.findByStatusAndDeletedFalse(OrderStatus.COMPLETED),
                "EXPORT"));
        items.addAll(buildOrderNotifications(exportOrderRepository.findByStatusAndDeletedFalse(OrderStatus.CANCELLED),
                "EXPORT"));

        items.addAll(buildLowStockNotifications());

        items.sort(this::compareNotifications);
        notifications = items.stream().limit(20).toList();
    }

    public List<BellNotificationDto> getNotifications() {
        return notifications;
    }

    public int getLowStockCount() {
        return notifications.size();
    }

    private int compareNotifications(BellNotificationDto left, BellNotificationDto right) {
        int timeComparison = compareDescending(left.getOccurredAt(), right.getOccurredAt());
        if (timeComparison != 0) {
            return timeComparison;
        }
        return compareDescending(left.getId(), right.getId());
    }

    private <T extends Comparable<? super T>> int compareDescending(T left, T right) {
        if (left == null && right == null)
            return 0;
        if (left == null)
            return 1;
        if (right == null)
            return -1;
        return right.compareTo(left);
    }

    private List<BellNotificationDto> buildLowStockNotifications() {
        return productRepository.findLowStockProducts().stream()
                .map(this::toLowStockNotification)
                .toList();
    }

    private BellNotificationDto toLowStockNotification(Product product) {
        Integer quantity = product.getQuantity() != null ? product.getQuantity() : 0;
        Integer minQuantity = product.getMinQuantity() != null ? product.getMinQuantity() : 0;
        LocalDateTime occurredAt = product.getUpdatedAt() != null ? product.getUpdatedAt()
                : LocalDateTime.now(VIETNAM_ZONE);
        return new BellNotificationDto(
                product.getId(),
                "LOW_STOCK",
                "Sản phẩm sắp hết",
                product.getName(),
                product.getCode(),
                "Sắp hết hàng",
                "bi-exclamation-triangle-fill",
                "badge-low",
                quantity,
                minQuantity,
                occurredAt,
                occurredAt.format(NOTIFICATION_TIME_FORMATTER),
                "/products/" + product.getId());
    }

    private List<BellNotificationDto> buildOrderNotifications(List<? extends AbstractOrder> orders, String orderType) {
        return orders.stream()
                .map(order -> toOrderNotification(order, orderType))
                .toList();
    }

    private BellNotificationDto toOrderNotification(AbstractOrder order, String orderType) {
        String type = orderType + "_" + order.getStatus().name();
        String orderLabel = "IMPORT".equals(orderType) ? "nhập kho" : "xuất kho";
        String title = switch (order.getStatus()) {
            case PENDING -> "Phiếu " + orderLabel + " đang chờ xử lý";
            case COMPLETED -> "Phiếu " + orderLabel + " đã hoàn thành";
            case CANCELLED -> "Phiếu " + orderLabel + " đã hủy";
        };

        String badgeClass = switch (order.getStatus()) {
            case PENDING -> "badge-pending";
            case COMPLETED -> "badge-completed";
            case CANCELLED -> "badge-cancelled";
        };

        String icon = switch (order.getStatus()) {
            case PENDING -> "bi-clock-history";
            case COMPLETED -> "bi-check-circle-fill";
            case CANCELLED -> "bi-x-circle-fill";
        };

        String href = "/" + ("IMPORT".equals(orderType) ? "imports" : "exports") + "/" + order.getId();
        LocalDateTime occurredAt = order.getUpdatedAt() != null ? order.getUpdatedAt()
                : LocalDateTime.now(VIETNAM_ZONE);
        return new BellNotificationDto(
                order.getId(),
                type,
                title,
                "Mã phiếu: " + order.getCode(),
                order.getCode(),
                order.getStatus().getDisplayName(),
                icon,
                badgeClass,
                null,
                null,
                occurredAt,
                occurredAt.format(NOTIFICATION_TIME_FORMATTER),
                href);
    }
}