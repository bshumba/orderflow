
package com.brianshumba.orderflow.product.domain.model;

import com.brianshumba.orderflow.product.domain.exception.InsufficientStockException;
import com.brianshumba.orderflow.product.domain.exception.InvalidStockOperationException;

import java.util.Objects;
import java.util.UUID;

public class Inventory {

    private final UUID productId;

    private int onHandQuantity;
    private int reservedQuantity;

    public Inventory(UUID productId) {
        this.productId = Objects.requireNonNull(
            productId,
            "Product ID cannot be null"
        );

        this.onHandQuantity = 0;
        this.reservedQuantity = 0;
    }

    public void receiveStock(int quantity) {
        validatePositiveQuantity(quantity);

        this.onHandQuantity = Math.addExact(
            this.onHandQuantity,
            quantity
        );
    }

    public void reserveStock(int quantity) {
        validatePositiveQuantity(quantity);

        if (quantity > getAvailableQuantity()) {
            throw new InsufficientStockException(
                "Insufficient available stock"
            );
        }

        this.reservedQuantity = Math.addExact(
            this.reservedQuantity,
            quantity
        );
    }

    public void releaseReservation(int quantity) {
        validatePositiveQuantity(quantity);

        if (quantity > reservedQuantity) {
            throw new InvalidStockOperationException(
                "Cannot release more stock than reserved"
            );
        }

        this.reservedQuantity -= quantity;
    }

    public void fulfillReservation(int quantity) {
        validatePositiveQuantity(quantity);

        if (quantity > reservedQuantity) {
            throw new InvalidStockOperationException(
                "Cannot fulfill more stock than reserved"
            );
        }

        this.reservedQuantity -= quantity;
        this.onHandQuantity -= quantity;
    }

    public int getAvailableQuantity() {
        return onHandQuantity - reservedQuantity;
    }

    private void validatePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw new InvalidStockOperationException(
                "Quantity must be greater than zero"
            );
        }
    }

    public void adjustStock(int newQuantity) {

        if (newQuantity < 0) {
            throw new InvalidStockOperationException(
                "Stock quantity cannot be negative"
            );
        }

        if (newQuantity < reservedQuantity) {
            throw new InvalidStockOperationException(
                "Cannot adjust stock below reserved quantity"
            );
        }

        this.onHandQuantity = newQuantity;
    }
    public UUID getProductId() {
        return productId;
    }

    public int getOnHandQuantity() {
        return onHandQuantity;
    }

    public int getReservedQuantity() {
        return reservedQuantity;
    }

    public String toString() {
        return "Inventory{" +
            "productId=" + productId +
            ", onHandQuantity=" + onHandQuantity +
            ", reservedQuantity=" + reservedQuantity +
            '}';
    }
}
