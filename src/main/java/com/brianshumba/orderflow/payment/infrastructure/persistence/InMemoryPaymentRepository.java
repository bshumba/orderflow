
package com.brianshumba.orderflow.payment.infrastructure.persistence;

import com.brianshumba.orderflow.payment.domain.model.Payment;
import com.brianshumba.orderflow.payment.domain.repository.PaymentRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<UUID, Payment> payments = new HashMap<>();

    @Override
    public void save(Payment payment) {
        Objects.requireNonNull(payment, "Payment cannot be null");

        payments.put(payment.getId(), payment.copy());
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return Optional.ofNullable(payments.get(id))
            .map(Payment::copy);
    }

    @Override
    public List<Payment> findByOrderId(UUID orderId) {
        return payments.values()
            .stream()
            .filter(payment -> payment.getOrderId().equals(orderId))
            .map(Payment::copy)
            .toList();
    }

    @Override
    public List<Payment> findAll() {
        return payments.values()
            .stream()
            .map(Payment::copy)
            .toList();
    }
}
