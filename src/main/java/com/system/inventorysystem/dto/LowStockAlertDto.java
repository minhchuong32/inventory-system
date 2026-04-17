package com.system.inventorysystem.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LowStockAlertDto {
    private final Long id;
    private final String code;
    private final String name;
    private final Integer quantity;
    private final Integer minQuantity;
    private final String unitName;
}