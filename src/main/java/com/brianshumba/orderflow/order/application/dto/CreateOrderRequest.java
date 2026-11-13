
package com.brianshumba.orderflow.order.application.dto;

import java.util.List;
import java.util.UUID;

public record CreateOrderRequest(
    UUID customerId,
    List<CreateOrderItemRequest> items
) {
    public CreateOrderRequest {
        if (items != null) {
            items = List.copyOf(items);
        }
    }
}
