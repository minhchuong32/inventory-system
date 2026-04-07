package com.system.inventorysystem.strategy.discount;

import java.math.BigDecimal;

/**
 * Strategy Pattern — Discount Strategy Interface
 *
 * Mỗi loại khách hàng (RETAIL, WHOLESALE, VIP) có một chiến lược
 * tính giảm giá riêng. ExportService chỉ cần gọi interface này,
 * không cần biết cụ thể loại khách hàng là gì.
 */
public interface DiscountStrategy {

    /**
     * Tên loại khách hàng áp dụng strategy này.
     * Khớp với giá trị Customer.customerType
     */
    String getCustomerType();

    /**
     * Tỉ lệ giảm giá (%).
     * VD: 5.0 = giảm 5%
     */
    double getDiscountPercent();

    /**
     * Tính số tiền được giảm từ tổng tiền.
     *
     * @param totalAmount tổng tiền trước giảm giá
     * @return số tiền được giảm
     */
    BigDecimal calculateDiscount(BigDecimal totalAmount);

    /**
     * Tính tổng tiền sau khi đã giảm giá.
     *
     * @param totalAmount tổng tiền trước giảm giá
     * @return tổng tiền sau giảm
     */
    BigDecimal applyDiscount(BigDecimal totalAmount);

    /**
     * Mô tả chiến lược giảm giá (hiển thị trên UI).
     */
    String getDescription();
}
