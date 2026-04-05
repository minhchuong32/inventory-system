package com.system.inventorysystem.strategy.discount;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory cho DiscountStrategy.
 *
 * Spring tự động inject tất cả các bean DiscountStrategy vào List.
 * Factory tra cứu strategy phù hợp theo customerType.
 *
 * Sử dụng:
 *   DiscountStrategy strategy = factory.getStrategy("VIP");
 *   BigDecimal discount = strategy.calculateDiscount(totalAmount);
 */
@Component
@Slf4j
public class DiscountStrategyFactory {

    private final Map<String, DiscountStrategy> strategyMap;
    private final DiscountStrategy defaultStrategy;

    public DiscountStrategyFactory(List<DiscountStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        s -> s.getCustomerType().toUpperCase(),
                        Function.identity()
                ));
        // Mặc định dùng RETAIL nếu không tìm thấy
        this.defaultStrategy = strategies.stream()
                .filter(s -> "RETAIL".equalsIgnoreCase(s.getCustomerType()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Không tìm thấy RetailDiscountStrategy trong Spring context"));

        log.info("[DiscountStrategyFactory] Đã đăng ký {} chiến lược giảm giá: {}",
                strategyMap.size(), strategyMap.keySet());
    }

    /**
     * Lấy strategy theo loại khách hàng.
     * Nếu không tìm thấy → trả về RETAIL (không giảm giá).
     *
     * @param customerType "RETAIL" | "WHOLESALE" | "VIP"
     */
    public DiscountStrategy getStrategy(String customerType) {
        if (customerType == null || customerType.isBlank()) {
            log.warn("[DiscountStrategyFactory] customerType null → dùng RETAIL mặc định");
            return defaultStrategy;
        }
        DiscountStrategy strategy = strategyMap.get(customerType.toUpperCase());
        if (strategy == null) {
            log.warn("[DiscountStrategyFactory] Không tìm thấy strategy cho '{}' → dùng RETAIL mặc định", customerType);
            return defaultStrategy;
        }
        return strategy;
    }

    /**
     * Lấy tất cả strategies (dùng cho UI dropdown).
     */
    public Map<String, DiscountStrategy> getAllStrategies() {
        return strategyMap;
    }
}
