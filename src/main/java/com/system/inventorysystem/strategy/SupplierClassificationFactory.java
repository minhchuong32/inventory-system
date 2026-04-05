package com.system.inventorysystem.strategy;

import com.system.inventorysystem.enums.SupplierType;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SupplierClassificationFactory {

    private final Map<SupplierType, SupplierClassificationStrategy> strategies = new HashMap<>();

    public SupplierClassificationFactory(List<SupplierClassificationStrategy> strategyList) {
        for (SupplierClassificationStrategy strategy : strategyList) {
            strategies.put(strategy.getType(), strategy);
        }
    }

    public SupplierClassificationStrategy getStrategy(SupplierType type) {
        SupplierClassificationStrategy strategy = strategies.get(type);
        if (strategy == null) {
            throw new RuntimeException("Không tìm thấy chiến lược cho loại: " + type);
        }
        return strategy;
    }
}
