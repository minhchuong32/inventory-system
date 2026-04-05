package com.system.inventorysystem.observer;

public interface OrderEventListener {
    void onOrderEvent(OrderEvent event);
    boolean supports(OrderEvent event);      // filter sự kiện mình quan tâm
}