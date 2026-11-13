
package com.brianshumba.orderflow.order.domain.model;

import com.brianshumba.orderflow.order.domain.exception.InvalidOrderException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

public record OrderItem(
    UUID productId,
    int quantity,
    BigDecimal unitPrice
) {

    public OrderItem {

        Objects.requireNonNull(
            productId,
            "Product ID cannot be null"
        );

        if (quantity <= 0) {
            throw new InvalidOrderException(
                "Order item quantity must be greater than zero"
            );
        }

        if (unitPrice == null || unitPrice.signum() < 0) {
            throw new InvalidOrderException(
                "Unit price cannot be null or negative"
            );
        }

        try {
            unitPrice = unitPrice.setScale(
                2,
                RoundingMode.UNNECESSARY
            );
        } catch (ArithmeticException exception) {
            throw new InvalidOrderException(
                "Unit price cannot exceed two decimal places"
            );
        }
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(
            BigDecimal.valueOf(quantity)
        );
    }
}
