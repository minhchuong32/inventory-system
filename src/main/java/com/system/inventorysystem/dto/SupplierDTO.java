package com.system.inventorysystem.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierDTO {
    private Long id;
    private String code;
    private String name;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private String taxCode;
    private String bankAccount;
    private String bankName;
    private BigDecimal creditLimit;
    private BigDecimal currentDebt;
    private BigDecimal remainingCredit;
    private Boolean status;

    private String supplierType;
    private String supplierTypeDisplayName;
    private double discountPercentage;
    private String classificationDescription;

    public boolean isOverCreditLimit() {
        if (creditLimit == null || currentDebt == null)
            return false;
        return currentDebt.compareTo(creditLimit) > 0;
    }
}
