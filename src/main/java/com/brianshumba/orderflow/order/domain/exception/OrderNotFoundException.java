package com.brianshumba.orderflow.order.domain.exception;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(UUID id) {
        super("Order not found with ID: " + id);
    }
}
