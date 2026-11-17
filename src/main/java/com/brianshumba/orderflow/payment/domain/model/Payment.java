
package com.brianshumba.orderflow.payment.domain.model;

import com.brianshumba.orderflow.payment.domain.exception.InvalidPaymentException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Payment {

    private final UUID id;
    private final UUID orderId;
    private final BigDecimal amount;
    private final PaymentMethod method;
    private final Instant createdAt;

    private PaymentStatus status;
    private String transactionReference;

    public Payment(
        UUID id,
        UUID orderId,
        BigDecimal amount,
        PaymentMethod method,
        Instant createdAt
    ) {
        this.id = Objects.requireNonNull(id, "Payment ID is required");

        this.orderId = Objects.requireNonNull(
            orderId, "Order ID is required"
        );

        this.method = Objects.requireNonNull(
            method, "Payment method is required"
        );

        this.createdAt = Objects.requireNonNull(
            createdAt, "Creation time is required"
        );

        if (amount == null || amount.signum() <= 0) {
            throw new InvalidPaymentException(
                "Payment amount must be greater than zero"
            );
        }

        try {
            this.amount = amount.setScale(
                2,
                RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException e) {
            throw new InvalidPaymentException(
                "Payment amount cannot exceed two decimal places"
            );
        }

        this.status = PaymentStatus.PENDING;
        this.transactionReference = null;
    }

    private Payment(
        UUID id,
        UUID orderId,
        BigDecimal amount,
        PaymentMethod method,
        Instant createdAt,
        PaymentStatus status,
        String transactionReference
    ) {
        this(id, orderId, amount, method, createdAt);

        this.status = Objects.requireNonNull(status);
        this.transactionReference = transactionReference;
    }

    public Payment copy() {
        return new Payment(
            id,
            orderId,
            amount,
            method,
            createdAt,
            status,
            transactionReference
        );
    }

    public void markSuccessful(String reference) {

        if (status != PaymentStatus.PENDING) {
            throw new InvalidPaymentException(
                "Only pending payments can succeed"
            );
        }

        if (reference == null || reference.isBlank()) {
            throw new InvalidPaymentException(
                "Transaction reference is required"
            );
        }

        this.transactionReference = reference.trim();
        this.status = PaymentStatus.SUCCESSFUL;
    }

    public void markFailed() {

        if (status != PaymentStatus.PENDING) {
            throw new InvalidPaymentException(
                "Only pending payments can fail"
            );
        }

        this.status = PaymentStatus.FAILED;
    }

    public void refund() {

        if (status != PaymentStatus.SUCCESSFUL) {
            throw new InvalidPaymentException(
                "Only successful payments can be refunded"
            );
        }

        this.status = PaymentStatus.REFUNDED;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getTransactionReference() {
        return transactionReference;
    }
}
