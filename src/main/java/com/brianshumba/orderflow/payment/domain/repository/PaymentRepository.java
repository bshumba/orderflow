package com.brianshumba.orderflow.payment.domain.repository;

import com.brianshumba.orderflow.payment.domain.model.Payment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

    void save(Payment payment);

    Optional<Payment> findById(UUID id);

    List<Payment> findByOrderId(UUID orderId);

    List<Payment> findAll();
}
