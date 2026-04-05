package com.system.inventorysystem.strategy.discount;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Strategy: Khách hàng VIP
 * Được giảm 10% trên tổng đơn hàng.
 */
@Component
public class VipDiscountStrategy implements DiscountStrategy {

    private static final double DISCOUNT_PERCENT = 10.0;

    @Override
    public String getCustomerType() {
        return "VIP";
    }

    @Override
    public double getDiscountPercent() {
        return DISCOUNT_PERCENT;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal totalAmount) {
        if (totalAmount == null) return BigDecimal.ZERO;
        return totalAmount
                .multiply(BigDecimal.valueOf(DISCOUNT_PERCENT))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal totalAmount) {
        if (totalAmount == null) return BigDecimal.ZERO;
        return totalAmount.subtract(calculateDiscount(totalAmount))
                          .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getDescription() {
        return "Khách VIP - Giảm 10% trên tổng đơn hàng";
    }
}
