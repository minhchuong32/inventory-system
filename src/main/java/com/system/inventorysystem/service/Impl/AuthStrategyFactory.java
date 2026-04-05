package com.system.inventorysystem.service.Impl;

import com.system.inventorysystem.enums.AuthType;
import com.system.inventorysystem.service.AuthStrategy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component
public class AuthStrategyFactory {

    private final Map<AuthType, AuthStrategy> strategies = new HashMap<>();

    public AuthStrategyFactory(List<AuthStrategy> strategyList) {

        for (AuthStrategy strategy : strategyList) {
            strategies.put(strategy.getAuthType(), strategy);
        }
    }

    public AuthStrategy getStrategy(AuthType type) {

        AuthStrategy strategy = strategies.get(type);

        if (strategy == null) {
            throw new RuntimeException("Unsupported auth type: " + type);
        }

        return strategy;
    }
}
