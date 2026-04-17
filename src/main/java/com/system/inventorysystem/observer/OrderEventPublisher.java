package com.system.inventorysystem.observer;

public interface OrderEventPublisher {
    void subscribe(OrderEventListener listener);

    void unsubscribe(OrderEventListener listener);

    void notifyListeners(OrderEvent event);
}