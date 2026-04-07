package com.system.inventorysystem.strategy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.system.inventorysystem.entity.Supplier;
import com.system.inventorysystem.enums.SupplierType;

@Component
public class RegularSupplierStrategy implements SupplierClassificationStrategy {
    private static final BigDecimal BASE_CREDIT_LIMIT = new BigDecimal("200000000");
    private static final double DISCOUNT_PERCENTAGE = 5.0;

    @Override
    public SupplierType getType() {
        return SupplierType.REGULAR;
    }

    @Override
    public BigDecimal calculateCreditLimit(Supplier supplier) {
        BigDecimal currentDebt = supplier.getCurrentDebt() != null ? supplier.getCurrentDebt() : BigDecimal.ZERO;
        return BASE_CREDIT_LIMIT.subtract(currentDebt);
    }

    @Override
    public double getDiscountPercentage() {
        return DISCOUNT_PERCENTAGE;
    }

    @Override
    public String getDescription() {
        return "Nhà cung cấp thông thường: Hạn mức 200 triệu, chiết khấu 5%";

    }

}
