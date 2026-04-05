package com.system.inventorysystem.observer;

public interface OrderEventPublisher {
    void publish(OrderEvent event);
}