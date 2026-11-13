
package com.brianshumba.orderflow.order.application.service;

import com.brianshumba.orderflow.customer.domain.exception.CustomerNotFoundException;
import com.brianshumba.orderflow.customer.domain.model.Customer;
import com.brianshumba.orderflow.customer.domain.repository.CustomerRepository;
import com.brianshumba.orderflow.order.application.dto.CreateOrderItemRequest;
import com.brianshumba.orderflow.order.application.dto.CreateOrderRequest;
import com.brianshumba.orderflow.order.domain.exception.InvalidOrderException;
import com.brianshumba.orderflow.order.domain.exception.OrderNotFoundException;
import com.brianshumba.orderflow.order.domain.model.Order;
import com.brianshumba.orderflow.order.domain.model.OrderItem;
import com.brianshumba.orderflow.order.domain.model.OrderStatus;
import com.brianshumba.orderflow.order.domain.repository.OrderRepository;
import com.brianshumba.orderflow.product.application.service.InventoryService;
import com.brianshumba.orderflow.product.domain.exception.ProductNotFoundException;
import com.brianshumba.orderflow.product.domain.model.Inventory;
import com.brianshumba.orderflow.product.domain.model.Product;
import com.brianshumba.orderflow.product.domain.repository.ProductRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public OrderService(
        OrderRepository orderRepository,
        CustomerRepository customerRepository,
        ProductRepository productRepository,
        InventoryService inventoryService
    ) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.customerRepository = Objects.requireNonNull(customerRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
        this.inventoryService = Objects.requireNonNull(inventoryService);
    }

    public Order createOrder(CreateOrderRequest request) {

        Objects.requireNonNull(
            request,
            "CreateOrderRequest cannot be null"
        );

        validateCustomer(request.customerId());

        List<OrderItem> items = prepareOrderItems(request.items());

        validateStockAvailability(items);

        Order order = new Order(
            UUID.randomUUID(),
            request.customerId(),
            items,
            Instant.now()
        );

        List<OrderItem> reservedItems = new ArrayList<>();

        try {
            for (OrderItem item : items) {

                inventoryService.reserveStock(
                    item.productId(),
                    item.quantity()
                );

                reservedItems.add(item);
            }

            order.markReserved();
            orderRepository.save(order);

            return order;

        } catch (RuntimeException failure) {

            for (int i = reservedItems.size() - 1; i >= 0; i--) {

                OrderItem item = reservedItems.get(i);

                try {
                    inventoryService.releaseReservation(
                        item.productId(),
                        item.quantity()
                    );
                } catch (RuntimeException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
            }

            throw failure;
        }
    }

    public Order fulfillOrder(UUID orderId) {

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new InvalidOrderException(
                "Only reserved orders can be fulfilled"
            );
        }

        List<OrderItem> fulfilledItems = new ArrayList<>();

        try {
            for (OrderItem item : order.getItems()) {

                inventoryService.fulfillReservation(
                    item.productId(),
                    item.quantity()
                );

                fulfilledItems.add(item);
            }

            order.fulfill();
            orderRepository.save(order);

        } catch (RuntimeException failure) {

            for (int i = fulfilledItems.size() - 1; i >= 0; i--) {

                OrderItem item = fulfilledItems.get(i);

                try {
                    inventoryService.receiveStock(
                        item.productId(),
                        item.quantity()
                    );

                    inventoryService.reserveStock(
                        item.productId(),
                        item.quantity()
                    );

                } catch (RuntimeException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
            }

            throw failure;
        }

        return order;
    }

    public Order cancelOrder(UUID orderId) {

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new InvalidOrderException(
                "Only reserved orders can be cancelled"
            );
        }

        List<OrderItem> releasedItems = new ArrayList<>();

        try {
            for (OrderItem item : order.getItems()) {

                inventoryService.releaseReservation(
                    item.productId(),
                    item.quantity()
                );

                releasedItems.add(item);
            }

            order.cancel();
            orderRepository.save(order);

        } catch (RuntimeException failure) {

            for (int i = releasedItems.size() - 1; i >= 0; i--) {

                OrderItem item = releasedItems.get(i);

                try {
                    inventoryService.reserveStock(
                        item.productId(),
                        item.quantity()
                    );
                } catch (RuntimeException rollbackFailure) {
                    failure.addSuppressed(rollbackFailure);
                }
            }

            throw failure;
        }

        return order;
    }

    private void validateCustomer(UUID customerId) {

        if (customerId == null) {
            throw new InvalidOrderException(
                "Customer ID is required"
            );
        }

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new CustomerNotFoundException(customerId));

        if (!customer.isActive()) {
            throw new InvalidOrderException(
                "Cannot create orders for inactive customers"
            );
        }
    }


    private List<OrderItem> prepareOrderItems(
        List<CreateOrderItemRequest> requestedItems
    ) {

        if (requestedItems == null || requestedItems.isEmpty()) {
            throw new InvalidOrderException(
                "An order must contain at least one item"
            );
        }

        Set<UUID> productIds = new HashSet<>();
        List<OrderItem> orderItems = new ArrayList<>();

        for (CreateOrderItemRequest requestedItem : requestedItems) {

            if (requestedItem == null) {
                throw new InvalidOrderException(
                    "Order item cannot be null"
                );
            }

            UUID productId = requestedItem.productId();

            if (productId == null) {
                throw new InvalidOrderException(
                    "Product ID is required"
                );
            }

            if (!productIds.add(productId)) {
                throw new InvalidOrderException(
                    "Duplicate product in order: " + productId
                );
            }

            Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

            if (!product.isActive()) {
                throw new InvalidOrderException(
                    "Cannot order an inactive product: " + productId
                );
            }

            OrderItem orderItem = new OrderItem(
                product.getId(),
                requestedItem.quantity(),
                product.getPrice()
            );

            orderItems.add(orderItem);
        }

        return List.copyOf(orderItems);
    }

    private void validateStockAvailability(List<OrderItem> items) {

        for (OrderItem item : items) {

            Inventory inventory = inventoryService.getInventory(
                item.productId()
            );

            if (inventory.getAvailableQuantity() < item.quantity()) {
                throw new InvalidOrderException(
                    "Insufficient stock for product: " + item.productId()
                );
            }
        }
    }
}
