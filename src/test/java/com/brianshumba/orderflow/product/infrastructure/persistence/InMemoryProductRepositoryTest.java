
package com.brianshumba.orderflow.product.infrastructure.persistence;

import com.brianshumba.orderflow.product.domain.model.Product;
import com.brianshumba.orderflow.product.domain.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryProductRepositoryTest {

    private ProductRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryProductRepository();
    }

    @Test
    void shouldSaveAndFindProductById() {
        Product product = createProduct("LAP-001");

        repository.save(product);

        Product found = repository.findById(product.getId())
            .orElseThrow();

        assertEquals(product.getId(), found.getId());
        assertEquals(product.getSku(), found.getSku());
    }

    @Test
    void shouldFindProductBySku() {
        Product product = createProduct("LAP-001");

        repository.save(product);

        Product found = repository.findBySku("lap-001")
            .orElseThrow();

        assertEquals(product.getId(), found.getId());
    }

    @Test
    void shouldReturnEmptyWhenProductDoesNotExist() {
        assertTrue(
            repository.findById(UUID.randomUUID()).isEmpty()
        );
    }

    @Test
    void shouldRejectDuplicateSku() {
        Product first = createProduct("LAP-001");
        Product second = createProduct("LAP-001");

        repository.save(first);

        assertThrows(
            IllegalArgumentException.class,
            () -> repository.save(second)
        );

        assertEquals(1, repository.findAll().size());
    }

    @Test
    void shouldReturnAllSavedProducts() {
        Product first = createProduct("LAP-001");
        Product second = createProduct("LAP-002");

        repository.save(first);
        repository.save(second);

        assertEquals(2, repository.findAll().size());
    }

    @Test
    void shouldDeleteExistingProduct() {
        Product product = createProduct("LAP-001");
        repository.save(product);

        boolean deleted = repository.deleteById(product.getId());

        assertTrue(deleted);
        assertTrue(repository.findById(product.getId()).isEmpty());
    }

    @Test
    void shouldReturnFalseWhenDeletingUnknownProduct() {
        assertFalse(
            repository.deleteById(UUID.randomUUID())
        );
    }

    @Test
    void shouldReturnTrueWhenSkuExists() {
        Product product = createProduct("LAP-001");
        repository.save(product);

        assertTrue(repository.existsBySku("lap-001"));
    }

    @Test
    void shouldReturnAllProductsAfterClearingReturnedList() {
        Product first = createProduct("LAP-001");
        Product second = createProduct("LAP-002");

        repository.save(first);
        repository.save(second);

        List<Product> allProducts = repository.findAll();
        allProducts.clear();

        assertEquals(2, repository.findAll().size());
    }

    private Product createProduct(String sku) {
        return new Product(
            UUID.randomUUID(),
            sku,
            "Dell Latitude",
            new BigDecimal("450.00")
        );
    }

}
