package com.system.inventorysystem.enums;

public enum SupplierType {
    VIP("VIP"),
    REGULAR("Thường"),
    NEW("Mới");

    private final String displayName;

    SupplierType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
