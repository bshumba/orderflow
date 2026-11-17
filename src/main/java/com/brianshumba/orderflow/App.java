
package com.brianshumba.orderflow;

import java.math.BigDecimal;
import java.util.List;

import com.brianshumba.orderflow.customer.application.dto.CreateCustomerRequest;
import com.brianshumba.orderflow.customer.application.service.CustomerService;
import com.brianshumba.orderflow.customer.domain.repository.CustomerRepository;
import com.brianshumba.orderflow.customer.infrastructure.persistence.InMemoryCustomerRepository;

import com.brianshumba.orderflow.order.application.dto.CreateOrderItemRequest;
import com.brianshumba.orderflow.order.application.dto.CreateOrderRequest;
import com.brianshumba.orderflow.order.application.service.OrderService;
import com.brianshumba.orderflow.order.domain.repository.OrderRepository;
import com.brianshumba.orderflow.order.infrastructure.persistence.InMemoryOrderRepository;

import com.brianshumba.orderflow.payment.application.service.PaymentService;
import com.brianshumba.orderflow.payment.domain.exception.InvalidPaymentException;
import com.brianshumba.orderflow.payment.domain.model.Payment;
import com.brianshumba.orderflow.payment.domain.model.PaymentMethod;
import com.brianshumba.orderflow.payment.domain.repository.PaymentRepository;
import com.brianshumba.orderflow.payment.infrastructure.persistence.InMemoryPaymentRepository;
import com.brianshumba.orderflow.payment.infrastructure.processing.CashPaymentProcessor;
import com.brianshumba.orderflow.payment.infrastructure.processing.EcoCashPaymentProcessor;
import com.brianshumba.orderflow.payment.infrastructure.processing.CardPaymentProcessor;

import com.brianshumba.orderflow.product.application.dto.CreateProductRequest;
import com.brianshumba.orderflow.product.application.service.InventoryService;
import com.brianshumba.orderflow.product.application.service.ProductService;
import com.brianshumba.orderflow.product.domain.repository.InventoryRepository;
import com.brianshumba.orderflow.product.domain.repository.ProductRepository;
import com.brianshumba.orderflow.product.infrastructure.persistence.InMemoryInventoryRepository;
import com.brianshumba.orderflow.product.infrastructure.persistence.InMemoryProductRepository;

public class App {

    public static void main(String[] args) {

        // 1. Repositories
        ProductRepository products = new InMemoryProductRepository();
        InventoryRepository inventories = new InMemoryInventoryRepository();
        CustomerRepository customers = new InMemoryCustomerRepository();
        OrderRepository orders = new InMemoryOrderRepository();
        PaymentRepository payments = new InMemoryPaymentRepository();

        // 2. Services
        ProductService productService = new ProductService(products);
        InventoryService inventoryService =
            new InventoryService(inventories, products);

        CustomerService customerService = new CustomerService(customers);

        OrderService orderService = new OrderService(
            orders, customers, products, inventoryService
        );

        PaymentService paymentService = new PaymentService(
            payments,
            orders,
            List.of(
                new CashPaymentProcessor(),
                new EcoCashPaymentProcessor(),
                new CardPaymentProcessor()
            )
        );

        // 3. Sample data
        var laptop = productService.createProduct(
            new CreateProductRequest(
                "LAP-001", "Dell Latitude", new BigDecimal("450.00")
            )
        );

        var yoga = productService.createProduct(
            new CreateProductRequest(
                "LAP-002", "Lenovo Yoga", new BigDecimal("400.00")
            )
        );

        var customer = customerService.createCustomer(
            new CreateCustomerRequest(
                "John", "Doe", "john@example.com", "0771234567"
            )
        );

        inventoryService.receiveStock(laptop.getId(), 10);
        inventoryService.receiveStock(yoga.getId(), 5);

        // 4. Create order
        var order = orderService.createOrder(
            new CreateOrderRequest(
                customer.getId(),
                List.of(
                    new CreateOrderItemRequest(laptop.getId(), 2),
                    new CreateOrderItemRequest(yoga.getId(), 1)
                )
            )
        );

        System.out.println("Order: " + order.getId());
        System.out.println("Total: $" + order.calculateTotal());
        System.out.println("Status: " + order.getStatus());

        // 5. Process payment
        var payment = paymentService.processPayment(
            order.getId(),
            PaymentMethod.ECOCASH
        );

        System.out.println("\nPayment method: " + payment.getMethod());
        System.out.println("Payment status: " + payment.getStatus());
        System.out.println("Reference: " + payment.getTransactionReference());

var failedOrder = orderService.createOrder(
    new CreateOrderRequest(
        customer.getId(),
        List.of(
            new CreateOrderItemRequest(laptop.getId(), 1)
        )
    )
);

Payment failedPayment = paymentService.processPayment(
    failedOrder.getId(),
    PaymentMethod.CARD
);

System.out.println("\n=== FAILED PAYMENT ===");
System.out.println("Status: " + failedPayment.getStatus());


System.out.println("\n=== PAYMENT REFUND ===");

Payment refunded = paymentService.refundPayment(payment.getId());

System.out.println("Payment ID: " + refunded.getId());
System.out.println("Refund status: " + refunded.getStatus());

try {
    paymentService.refundPayment(payment.getId());
} catch (InvalidPaymentException e) {
    System.out.println("Expected error: " + e.getMessage());
}

    }
}
