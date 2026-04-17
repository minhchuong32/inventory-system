package com.system.inventorysystem.strategy;

import java.math.BigDecimal;

import com.system.inventorysystem.entity.Supplier;
import com.system.inventorysystem.enums.SupplierType;

public interface SupplierClassificationStrategy {
    SupplierType getType();

    BigDecimal calculateCreditLimit(Supplier supplier);

    BigDecimal getRemainingCredit(Supplier supplier);

    boolean isOverCreditLimit(Supplier supplier);

    double getDiscountPercentage();

    String getDescription();
}
