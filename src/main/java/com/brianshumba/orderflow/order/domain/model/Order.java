
package com.brianshumba.orderflow.order.domain.model;

import com.brianshumba.orderflow.order.domain.exception.InvalidOrderException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Order {

    private final UUID id;
    private final UUID customerId;
    private final List<OrderItem> items;
    private final Instant createdAt;

    private OrderStatus status;

    public Order(
        UUID id,
        UUID customerId,
        List<OrderItem> items,
        Instant createdAt
    ) {
        this.id = Objects.requireNonNull(
            id, "Order ID cannot be null"
        );

        this.customerId = Objects.requireNonNull(
            customerId, "Customer ID cannot be null"
        );

        this.createdAt = Objects.requireNonNull(
            createdAt, "Creation time cannot be null"
        );

        if (items == null || items.isEmpty()) {
            throw new InvalidOrderException(
                "An order must contain at least one item"
            );
        }

        if (items.stream().anyMatch(Objects::isNull)) {
            throw new InvalidOrderException(
                "Order items cannot contain null values"
            );
        }

        this.items = List.copyOf(items);
        this.status = OrderStatus.DRAFT;
    }

    private Order(
        UUID id,
        UUID customerId,
        List<OrderItem> items,
        Instant createdAt,
        OrderStatus status
    ) {
        this(id, customerId, items, createdAt);
        this.status = Objects.requireNonNull(status);
    }

    public Order copy() {
        return new Order(
            id,
            customerId,
            items,
            createdAt,
            status
        );
    }

    public BigDecimal calculateTotal() {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(
                BigDecimal.ZERO.setScale(2),
                BigDecimal::add
            );
    }

    public int getTotalQuantity() {
        return items.stream()
            .mapToInt(OrderItem::quantity)
            .sum();
    }

    public void markReserved() {
        if (status != OrderStatus.DRAFT) {
            throw new InvalidOrderException(
                "Only draft orders can be reserved"
            );
        }

        this.status = OrderStatus.RESERVED;
    }

    public void cancel() {
        if (status != OrderStatus.DRAFT
                && status != OrderStatus.RESERVED) {
            throw new InvalidOrderException(
                "Only draft or reserved orders can be cancelled"
            );
        }

        this.status = OrderStatus.CANCELLED;
    }

    public void fulfill() {
        if (status != OrderStatus.RESERVED) {
            throw new InvalidOrderException(
                "Only reserved orders can be fulfilled"
            );
        }

        this.status = OrderStatus.FULFILLED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
