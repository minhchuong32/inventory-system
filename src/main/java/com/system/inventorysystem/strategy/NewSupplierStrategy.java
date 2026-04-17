package com.system.inventorysystem.strategy;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.system.inventorysystem.entity.Supplier;
import com.system.inventorysystem.enums.SupplierType;

@Component
public class NewSupplierStrategy implements SupplierClassificationStrategy {
    private static final BigDecimal BASE_CREDIT_LIMIT = new BigDecimal("50000000");
    private static final double DISCOUNT_PERCENTAGE = 0.0;

    @Override
    public SupplierType getType() {
        return SupplierType.NEW;
    }

    @Override
    public BigDecimal calculateCreditLimit(Supplier supplier) {
        return BASE_CREDIT_LIMIT;
    }

    @Override
    public BigDecimal getRemainingCredit(Supplier supplier) {
        BigDecimal currentDebt = supplier.getCurrentDebt() != null ? supplier.getCurrentDebt() : BigDecimal.ZERO;
        return BASE_CREDIT_LIMIT.subtract(currentDebt);
    }

    @Override
    public boolean isOverCreditLimit(Supplier supplier) {
        BigDecimal currentDebt = supplier.getCurrentDebt() != null ? supplier.getCurrentDebt() : BigDecimal.ZERO;
        return currentDebt.compareTo(BASE_CREDIT_LIMIT) > 0;
    }

    @Override
    public double getDiscountPercentage() {
        return DISCOUNT_PERCENTAGE;
    }

    @Override
    public String getDescription() {
        return "Nhà cung cấp mới: Hạn mức 50 triệu, chiết khấu 0%";
    }

}
