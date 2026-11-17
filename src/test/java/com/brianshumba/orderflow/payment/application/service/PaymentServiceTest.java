
package com.brianshumba.orderflow.payment.application.service;

import com.brianshumba.orderflow.order.domain.model.Order;
import com.brianshumba.orderflow.order.domain.model.OrderItem;
import com.brianshumba.orderflow.order.domain.repository.OrderRepository;
import com.brianshumba.orderflow.order.infrastructure.persistence.InMemoryOrderRepository;

import com.brianshumba.orderflow.payment.domain.exception.InvalidPaymentException;
import com.brianshumba.orderflow.payment.domain.model.Payment;
import com.brianshumba.orderflow.payment.domain.model.PaymentMethod;
import com.brianshumba.orderflow.payment.domain.model.PaymentStatus;
import com.brianshumba.orderflow.payment.domain.processing.PaymentProcessor;
import com.brianshumba.orderflow.payment.domain.processing.PaymentResult;
import com.brianshumba.orderflow.payment.domain.repository.PaymentRepository;
import com.brianshumba.orderflow.payment.infrastructure.persistence.InMemoryPaymentRepository;
import com.brianshumba.orderflow.payment.infrastructure.processing.CashPaymentProcessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private PaymentService paymentService;
    private Order order;

    @BeforeEach
    void setUp() {

        OrderRepository orderRepository =
            new InMemoryOrderRepository();

        PaymentRepository paymentRepository =
            new InMemoryPaymentRepository();

        // Fake processor to simulate a declined Card payment.
        PaymentProcessor decliningCard = new PaymentProcessor() {

            @Override
            public PaymentMethod supportedMethod() {
                return PaymentMethod.CARD;
            }

            @Override
            public PaymentResult process(Payment payment) {
                return PaymentResult.failure();
            }

            @Override
            public PaymentResult refund(Payment payment) {
                return PaymentResult.failure();
            }
        };

        paymentService = new PaymentService(
            paymentRepository,
            orderRepository,
            List.of(
                new CashPaymentProcessor(),
                decliningCard
            )
        );

        order = new Order(
            UUID.randomUUID(),
            UUID.randomUUID(),
            List.of(
                new OrderItem(
                    UUID.randomUUID(),
                    2,
                    new BigDecimal("100.00")
                )
            ),
            Instant.now()
        );

        order.markReserved();
        orderRepository.save(order);
    }

    @Test
    void shouldRejectDuplicateSuccessfulPayment() {

        Payment first = paymentService.processPayment(
            order.getId(),
            PaymentMethod.CASH
        );

        assertEquals(PaymentStatus.SUCCESSFUL, first.getStatus());

        assertThrows(
            InvalidPaymentException.class,
            () -> paymentService.processPayment(
                order.getId(),
                PaymentMethod.CASH
            )
        );
    }

    @Test
    void shouldAllowRetryAfterFailedPayment() {

        Payment failed = paymentService.processPayment(
            order.getId(),
            PaymentMethod.CARD
        );

        assertEquals(PaymentStatus.FAILED, failed.getStatus());

        Payment retry = paymentService.processPayment(
            order.getId(),
            PaymentMethod.CASH
        );

        assertEquals(PaymentStatus.SUCCESSFUL, retry.getStatus());
    }

    @Test
    void shouldRefundSuccessfulPaymentOnlyOnce() {

        Payment payment = paymentService.processPayment(
            order.getId(),
            PaymentMethod.CASH
        );

        Payment refunded = paymentService.refundPayment(
            payment.getId()
        );

        assertEquals(PaymentStatus.REFUNDED, refunded.getStatus());

        assertThrows(
            InvalidPaymentException.class,
            () -> paymentService.refundPayment(payment.getId())
        );
    }
}
