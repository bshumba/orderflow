package com.brianshumba.orderflow.payment.domain.processing;

import com.brianshumba.orderflow.payment.domain.model.Payment;
import com.brianshumba.orderflow.payment.domain.model.PaymentMethod;

public interface PaymentProcessor {

    PaymentMethod supportedMethod();

    PaymentResult process(Payment payment);

    PaymentResult refund(Payment payment);
}
