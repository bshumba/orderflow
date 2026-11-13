package com.brianshumba.orderflow;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.brianshumba.orderflow.customer.application.dto.CreateCustomerRequest;
import com.brianshumba.orderflow.customer.application.dto.UpdateCustomerRequest;
import com.brianshumba.orderflow.customer.application.service.CustomerService;
import com.brianshumba.orderflow.customer.domain.exception.DuplicateCustomerEmailException;
import com.brianshumba.orderflow.customer.domain.model.Customer;
import com.brianshumba.orderflow.customer.domain.repository.CustomerRepository;
import com.brianshumba.orderflow.customer.infrastructure.persistence.InMemoryCustomerRepository;
import com.brianshumba.orderflow.order.application.dto.CreateOrderItemRequest;
import com.brianshumba.orderflow.order.application.dto.CreateOrderRequest;
import com.brianshumba.orderflow.order.application.service.OrderService;
import com.brianshumba.orderflow.order.domain.exception.InvalidOrderException;
import com.brianshumba.orderflow.order.domain.model.Order;
import com.brianshumba.orderflow.order.domain.model.OrderItem;
import com.brianshumba.orderflow.order.domain.repository.OrderRepository;
import com.brianshumba.orderflow.order.infrastructure.persistence.InMemoryOrderRepository;
import com.brianshumba.orderflow.product.application.dto.CreateProductRequest;
import com.brianshumba.orderflow.product.application.service.InventoryService;
import com.brianshumba.orderflow.product.application.service.ProductService;
import com.brianshumba.orderflow.product.domain.exception.InsufficientStockException;
import com.brianshumba.orderflow.product.domain.exception.InvalidStockOperationException;
import com.brianshumba.orderflow.product.domain.exception.ProductNotFoundException;
import com.brianshumba.orderflow.product.domain.repository.InventoryRepository;
import com.brianshumba.orderflow.product.domain.repository.ProductRepository;
import com.brianshumba.orderflow.product.infrastructure.persistence.InMemoryInventoryRepository;
import com.brianshumba.orderflow.product.infrastructure.persistence.InMemoryProductRepository;

public class App {
    public static void main(String[] args) {

        ProductRepository productRepository =
            new InMemoryProductRepository();

        InventoryRepository inventoryRepository =
            new InMemoryInventoryRepository();

        ProductService productService =
            new ProductService(productRepository);

        InventoryService inventoryService =
            new InventoryService(
                inventoryRepository,
                productRepository
            );

        CustomerRepository customerRepository =
            new InMemoryCustomerRepository();

        CustomerService customerService =
            new CustomerService(customerRepository);

        OrderRepository orderRepository =
            new InMemoryOrderRepository();

        OrderService orderService = new OrderService(
            orderRepository,
            customerRepository,
            productRepository,
            inventoryService
        );

        try {
            CreateProductRequest productRequest1 = new CreateProductRequest(
                "LAP-001",
                "Dell Latitude",
                new BigDecimal("450.00")
            );

            CreateProductRequest productRequest2 = new CreateProductRequest(
                "LAP-002",
                "Lenovo Yoga",
                new BigDecimal("400.00")
            );

            var product1 = productService.createProduct(productRequest1);
            var product2 = productService.createProduct(productRequest2);

            CreateCustomerRequest customer1Request = new CreateCustomerRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "123-456-7890"
            );

            Customer customer1 = customerService.createCustomer(customer1Request);

            inventoryService.receiveStock(product1.getId(), 10);
            inventoryService.receiveStock(product2.getId(), 5);

            CreateOrderRequest orderRequest = new CreateOrderRequest(
                customer1.getId(),
                List.of(
                    new CreateOrderItemRequest(product1.getId(), 2),
                    new CreateOrderItemRequest(product2.getId(), 1)
                )
            );

            Order order = orderService.createOrder(orderRequest);

            System.out.println("Order ID: " + order.getId());
            System.out.println("Status: " + order.getStatus());
            System.out.println("Total: $" + order.calculateTotal());
            System.out.println("Quantity: " + order.getTotalQuantity());

            System.out.println(
                "Product 1 available stock: " +
                inventoryService.getInventory(product1.getId())
                    .getAvailableQuantity()
            );

            System.out.println(
                "Product 2 available stock: " +
                inventoryService.getInventory(product2.getId())
                    .getAvailableQuantity()
            );

            System.out.println("\n=== ORDER CANCELLATION ===");

            Order cancelledOrder = orderService.cancelOrder(order.getId());

            System.out.println("Status: " + cancelledOrder.getStatus());

            System.out.println(
                "Product 1 available: " +
                inventoryService.getInventory(product1.getId())
                    .getAvailableQuantity()
            );

            System.out.println(
                "Product 2 available: " +
                inventoryService.getInventory(product2.getId())
                    .getAvailableQuantity()
            );

            System.out.println("\n=== ORDER FULFILLMENT ===");

            CreateOrderRequest fulfillmentRequest = new CreateOrderRequest(
                customer1.getId(),
                List.of(
                    new CreateOrderItemRequest(product1.getId(), 2),
                    new CreateOrderItemRequest(product2.getId(), 1)
                )
            );

            Order fulfillmentOrder = orderService.createOrder(
                fulfillmentRequest
            );

            System.out.println(
                "Before fulfillment: " + fulfillmentOrder.getStatus()
            );

            Order fulfilledOrder = orderService.fulfillOrder(
                fulfillmentOrder.getId()
            );

            System.out.println(
                "After fulfillment: " + fulfilledOrder.getStatus()
            );

            var inventory1 = inventoryService.getInventory(product1.getId());
            var inventory2 = inventoryService.getInventory(product2.getId());

            System.out.println(
                "Product 1: on-hand=" + inventory1.getOnHandQuantity()
                + ", reserved=" + inventory1.getReservedQuantity()
                + ", available=" + inventory1.getAvailableQuantity()
            );

            System.out.println(
                "Product 2: on-hand=" + inventory2.getOnHandQuantity()
                + ", reserved=" + inventory2.getReservedQuantity()
                + ", available=" + inventory2.getAvailableQuantity()
            );

            System.out.println("\n=== Cancelling the already-fulfilled order ===");
            orderService.cancelOrder(fulfilledOrder.getId());

            System.out.println("ERROR: Fulfilled order was cancelled!");

            System.out.println("\n=== TEST 1: INSUFFICIENT STOCK ===");

            int beforeProduct1 = inventoryService
                .getInventory(product1.getId())
                .getAvailableQuantity();

            int beforeProduct2 = inventoryService
                .getInventory(product2.getId())
                .getAvailableQuantity();

            try {
                CreateOrderRequest insufficientStockRequest =
                    new CreateOrderRequest(
                        customer1.getId(),
                        List.of(
                            new CreateOrderItemRequest(product1.getId(), 3),
                            new CreateOrderItemRequest(product2.getId(), 10)
                        )
                    );

                orderService.createOrder(insufficientStockRequest);

                System.out.println("ERROR: Order should have failed!");

            } catch (InvalidOrderException | InsufficientStockException e) {
                System.out.println("Expected error: " + e.getMessage());
            }

            int afterProduct1 = inventoryService
                .getInventory(product1.getId())
                .getAvailableQuantity();

            int afterProduct2 = inventoryService
                .getInventory(product2.getId())
                .getAvailableQuantity();

            System.out.println("Product 1 before: " + beforeProduct1);
            System.out.println("Product 1 after:  " + afterProduct1);
            System.out.println("Product 2 before: " + beforeProduct2);
            System.out.println("Product 2 after:  " + afterProduct2);

            System.out.println(
                "Stock unchanged: " +
                (beforeProduct1 == afterProduct1
                    && beforeProduct2 == afterProduct2)
            );


            // ==============================================
            // TEST 2: DUPLICATE PRODUCTS
            // ==============================================

            System.out.println("\n=== TEST 2: DUPLICATE PRODUCTS ===");

            try {
                CreateOrderRequest duplicateProductRequest =
                    new CreateOrderRequest(
                        customer1.getId(),
                        List.of(
                            new CreateOrderItemRequest(product1.getId(), 2),
                            new CreateOrderItemRequest(product1.getId(), 3)
                        )
                    );

                orderService.createOrder(duplicateProductRequest);

                System.out.println("ERROR: Duplicate product was accepted!");

            } catch (InvalidOrderException e) {
                System.out.println("Expected error: " + e.getMessage());
            }

            System.out.println(
                "Product 1 available stock: " +
                inventoryService.getInventory(product1.getId())
                    .getAvailableQuantity()
            );

            System.out.println(
                "Total saved orders: " + orderRepository.findAll().size()
            );
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
