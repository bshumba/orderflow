
package com.brianshumba.orderflow.product.domain.model;

import com.brianshumba.orderflow.product.domain.exception.InvalidProductException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class Product {

    private final UUID id;
    private final String sku;

    private String name;
    private BigDecimal price;
    private boolean active;

    public Product(UUID id, String sku, String name, BigDecimal price) {
        this.id = Objects.requireNonNull(id, "Product ID cannot be null");
        this.sku = validateSku(sku);
        this.name = validateName(name);
        this.price = validatePrice(price);
        this.active = true;
    }

    private static String validateSku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new InvalidProductException("SKU cannot be blank");
        }

        return sku.trim().toUpperCase(Locale.ROOT);
    }

    private static String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidProductException("Product name cannot be blank");
        }

        return name.trim();
    }

    private static BigDecimal validatePrice(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new InvalidProductException("Product price cannot be negative or null");
        }

        try {
            return price.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new InvalidProductException(
                "Product price cannot have more than two decimal places"
            );
        }
    }

    public void updateDetails(String newName, BigDecimal newPrice) {

        String validatedName = validateName(newName);
        BigDecimal validatedPrice = validatePrice(newPrice);

        this.name = validatedName;
        this.price = validatedPrice;
    }

    public void rename(String newName) {
        this.name = validateName(newName);
    }

    public void changePrice(BigDecimal newPrice) {
        this.price = validatePrice(newPrice);
    }

    public void deactivate() {
        this.active = false;
    }

    public UUID getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public boolean isActive() {
        return active;
    }

    public String toString() {
        return "Product{" +
            "id=" + id +
            ", sku='" + sku + '\'' +
            ", name='" + name + '\'' +
            ", price=" + price +
            ", active=" + active +
            '}';
    }
}
