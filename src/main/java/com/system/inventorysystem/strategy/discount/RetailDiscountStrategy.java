package com.system.inventorysystem.strategy.discount;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Strategy: Khách hàng lẻ (RETAIL)
 * Không được giảm giá. Mua theo giá niêm yết.
 */
@Component
public class RetailDiscountStrategy implements DiscountStrategy {

    private static final double DISCOUNT_PERCENT = 0.0;

    @Override
    public String getCustomerType() {
        return "RETAIL";
    }

    @Override
    public double getDiscountPercent() {
        return DISCOUNT_PERCENT;
    }

    @Override
    public BigDecimal calculateDiscount(BigDecimal totalAmount) {
        if (totalAmount == null) return BigDecimal.ZERO;
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal applyDiscount(BigDecimal totalAmount) {
        if (totalAmount == null) return BigDecimal.ZERO;
        return totalAmount.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String getDescription() {
        return "Khách lẻ - Không giảm giá (0%)";
    }
}
