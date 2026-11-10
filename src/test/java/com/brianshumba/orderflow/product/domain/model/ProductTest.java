
package com.brianshumba.orderflow.product.domain.model;

import com.brianshumba.orderflow.product.domain.exception.InvalidProductException;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void shouldCreateValidProduct() {
        Product product = new Product(
            UUID.randomUUID(),
            "lap-001",
            "Dell Latitude",
            new BigDecimal("450.00")
        );

        assertEquals("LAP-001", product.getSku());
        assertEquals("Dell Latitude", product.getName());
        assertEquals(new BigDecimal("450.00"), product.getPrice());
        assertTrue(product.isActive());
    }

    @Test
    void shouldRejectBlankSku() {
        assertThrows(
            InvalidProductException.class,
            () -> new Product(
                UUID.randomUUID(),
                " ",
                "Dell Latitude",
                new BigDecimal("450.00")
            )
        );
    }

    @Test
    void shouldRejectNegativePrice() {
        assertThrows(
            InvalidProductException.class,
            () -> new Product(
                UUID.randomUUID(),
                "LAP-001",
                "Dell Latitude",
                new BigDecimal("-50.00")
            )
        );
    }

    @Test
    void shouldUpdateProductPrice() {
        Product product = new Product(
            UUID.randomUUID(),
            "LAP-001",
            "Dell Latitude",
            new BigDecimal("450.00")
        );

        product.changePrice(new BigDecimal("500.00"));

        assertEquals(
            new BigDecimal("500.00"),
            product.getPrice()
        );
    }

    @Test
    void shouldDeactivateProduct() {
        Product product = new Product(
            UUID.randomUUID(),
            "LAP-001",
            "Dell Latitude",
            new BigDecimal("450.00")
        );

        product.deactivate();

        assertFalse(product.isActive());
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(
            InvalidProductException.class,
            () -> new Product(
                UUID.randomUUID(),
                "LAP-001",
                "",
                new BigDecimal("450.00")
            )
        );
    }

    @Test
    void shouldUpdateNameCorrectly() {
        Product product = new Product(
            UUID.randomUUID(),
            "LAP-001",
            "Dell Latitude",
            new BigDecimal("450.00")
        );

        product.rename("Asus ROG");

        assertEquals(
            "Asus ROG",
            product.getName()
        );
    }

    @Test
    void shouldRejectUpdatePriceToNegative() {
        Product product = new Product(
            UUID.randomUUID(),
            "LAP-001",
            "Dell Latitude",
            new BigDecimal("450.00")
        );

        assertThrows(
            InvalidProductException.class,
            () -> product.changePrice(new BigDecimal("-400.00"))
        );

        assertEquals(
            new BigDecimal("450.00"),
            product.getPrice()
        );
    }
}
