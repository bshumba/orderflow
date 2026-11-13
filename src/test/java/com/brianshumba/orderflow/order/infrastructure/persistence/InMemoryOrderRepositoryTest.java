
package com.brianshumba.orderflow.order.infrastructure.persistence;

import com.brianshumba.orderflow.order.domain.model.Order;
import com.brianshumba.orderflow.order.domain.model.OrderItem;
import com.brianshumba.orderflow.order.domain.model.OrderStatus;
import com.brianshumba.orderflow.order.domain.repository.OrderRepository;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryOrderRepositoryTest {

    @Test
    void shouldNotStoreChangesUntilSaveIsCalled() {

        OrderRepository repository = new InMemoryOrderRepository();

        Order order = createOrder();
        repository.save(order);

        order.markReserved();

        Order storedOrder = repository.findById(order.getId())
            .orElseThrow();

        assertEquals(OrderStatus.DRAFT, storedOrder.getStatus());

        repository.save(order);

        Order updatedOrder = repository.findById(order.getId())
            .orElseThrow();

        assertEquals(OrderStatus.RESERVED, updatedOrder.getStatus());
    }

    @Test
    void shouldNotExposeStoredOrderForMutation() {

        OrderRepository repository = new InMemoryOrderRepository();

        Order order = createOrder();
        repository.save(order);

        Order retrieved = repository.findById(order.getId())
            .orElseThrow();

        retrieved.cancel();

        Order stored = repository.findById(order.getId())
            .orElseThrow();

        assertEquals(OrderStatus.DRAFT, stored.getStatus());
    }

    private Order createOrder() {
        return new Order(
            UUID.randomUUID(),
            UUID.randomUUID(),
            List.of(
                new OrderItem(
                    UUID.randomUUID(),
                    2,
                    new BigDecimal("450.00")
                )
            ),
            Instant.now()
        );
    }
}
