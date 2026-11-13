package com.brianshumba.orderflow.order.application.dto;

import java.util.UUID;

public record CreateOrderItemRequest(
    UUID productId,
    int quantity
) {}
