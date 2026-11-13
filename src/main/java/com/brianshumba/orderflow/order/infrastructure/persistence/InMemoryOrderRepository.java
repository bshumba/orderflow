
package com.brianshumba.orderflow.order.infrastructure.persistence;

import com.brianshumba.orderflow.order.domain.model.Order;
import com.brianshumba.orderflow.order.domain.repository.OrderRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class InMemoryOrderRepository implements OrderRepository {

    private final Map<UUID, Order> orders = new HashMap<>();

    @Override
    public void save(Order order) {
        Objects.requireNonNull(order, "Order cannot be null");

        orders.put(order.getId(), order.copy());
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return Optional.ofNullable(orders.get(id))
            .map(Order::copy);
    }

    @Override
    public List<Order> findByCustomerId(UUID customerId) {
        return orders.values()
            .stream()
            .filter(order ->
                order.getCustomerId().equals(customerId)
            )
            .map(Order::copy)
            .toList();
    }

    @Override
    public List<Order> findAll() {
        return orders.values()
            .stream()
            .map(Order::copy)
            .toList();
    }
}
