
package com.brianshumba.orderflow.payment.application.service;

import com.brianshumba.orderflow.order.domain.exception.OrderNotFoundException;
import com.brianshumba.orderflow.order.domain.model.Order;
import com.brianshumba.orderflow.order.domain.model.OrderStatus;
import com.brianshumba.orderflow.order.domain.repository.OrderRepository;

import com.brianshumba.orderflow.payment.domain.exception.InvalidPaymentException;
import com.brianshumba.orderflow.payment.domain.model.Payment;
import com.brianshumba.orderflow.payment.domain.model.PaymentMethod;
import com.brianshumba.orderflow.payment.domain.model.PaymentStatus;
import com.brianshumba.orderflow.payment.domain.processing.PaymentProcessor;
import com.brianshumba.orderflow.payment.domain.processing.PaymentResult;
import com.brianshumba.orderflow.payment.domain.repository.PaymentRepository;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final Map<PaymentMethod, PaymentProcessor> processors;

    public PaymentService(
        PaymentRepository paymentRepository,
        OrderRepository orderRepository,
        List<PaymentProcessor> availableProcessors
    ) {
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
        this.orderRepository = Objects.requireNonNull(orderRepository);

        Objects.requireNonNull(availableProcessors);

        this.processors = new EnumMap<>(PaymentMethod.class);

        for (PaymentProcessor processor : availableProcessors) {
            Objects.requireNonNull(processor);

            PaymentProcessor previous = processors.putIfAbsent(
                processor.supportedMethod(),
                processor
            );

            if (previous != null) {
                throw new IllegalArgumentException(
                    "Duplicate processor: " + processor.supportedMethod()
                );
            }
        }
    }

    public Payment processPayment(UUID orderId, PaymentMethod method) {

        if (orderId == null || method == null) {
            throw new InvalidPaymentException(
                "Order ID and payment method are required"
            );
        }

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() != OrderStatus.RESERVED) {
            throw new InvalidPaymentException(
                "Only reserved orders can be paid"
            );
        }

        List<Payment> previousPayments =
            paymentRepository.findByOrderId(orderId);

        boolean alreadyPaid = previousPayments.stream()
            .anyMatch(payment ->
                payment.getStatus() == PaymentStatus.SUCCESSFUL
                || payment.getStatus() == PaymentStatus.REFUNDED
            );

        if (alreadyPaid) {
            throw new InvalidPaymentException(
                "Order has already been paid or refunded"
            );
        }

        boolean pendingPaymentExists = previousPayments.stream()
            .anyMatch(payment ->
                payment.getStatus() == PaymentStatus.PENDING
            );

        if (pendingPaymentExists) {
            throw new InvalidPaymentException(
                "Order already has a pending payment"
            );
        }

        PaymentProcessor processor = processors.get(method);

        if (processor == null) {
            throw new InvalidPaymentException(
                "No processor registered for: " + method
            );
        }

        Payment payment = new Payment(
            UUID.randomUUID(),
            order.getId(),
            order.calculateTotal(),
            method,
            Instant.now()
        );

        // Save the attempt before contacting the processor.
        paymentRepository.save(payment);

        PaymentResult result = processor.process(payment.copy());

        if (result == null) {
            throw new InvalidPaymentException(
                "Processor returned no result"
            );
        }

        if (result.successful()) {
            payment.markSuccessful(result.reference());
        } else {
            payment.markFailed();
        }

        paymentRepository.save(payment);

        return payment;
    }

    public Payment refundPayment(UUID paymentId) {

        if (paymentId == null) {
            throw new InvalidPaymentException(
                "Payment ID is required"
            );
        }

        Payment payment = paymentRepository.findById(paymentId)
            .orElseThrow(() -> new InvalidPaymentException(
                "Payment not found: " + paymentId
            ));

        if (payment.getStatus() != PaymentStatus.SUCCESSFUL) {
            throw new InvalidPaymentException(
                "Only successful payments can be refunded"
            );
        }

        PaymentProcessor processor = processors.get(payment.getMethod());

        if (processor == null) {
            throw new InvalidPaymentException(
                "No refund processor registered"
            );
        }

        PaymentResult result = processor.refund(payment.copy());

        if (result == null || !result.successful()) {
            throw new InvalidPaymentException(
                "Refund was not confirmed"
            );
        }

        payment.refund();
        paymentRepository.save(payment);

        return payment;
    }

}
