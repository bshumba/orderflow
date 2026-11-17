
package com.brianshumba.orderflow.payment.infrastructure.processing;

import com.brianshumba.orderflow.payment.domain.model.Payment;
import com.brianshumba.orderflow.payment.domain.model.PaymentMethod;
import com.brianshumba.orderflow.payment.domain.processing.PaymentProcessor;
import com.brianshumba.orderflow.payment.domain.processing.PaymentResult;

import java.util.UUID;

public class CashPaymentProcessor implements PaymentProcessor {

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.CASH;
    }

    @Override
    public PaymentResult process(Payment payment) {
        String reference = "CASH-" + UUID.randomUUID();
        return PaymentResult.success(reference);
    }

    @Override
    public PaymentResult refund(Payment payment) {
        return PaymentResult.success(
            "REFUND-" + UUID.randomUUID()
        );
    }
}
