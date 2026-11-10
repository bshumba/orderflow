
package com.brianshumba.orderflow.product.infrastructure.persistence;

import com.brianshumba.orderflow.product.domain.model.Product;
import com.brianshumba.orderflow.product.domain.repository.ProductRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class InMemoryProductRepository implements ProductRepository {

    private final Map<UUID, Product> products = new HashMap<>();

    @Override
    public void save(Product product) {
        Objects.requireNonNull(product, "Product cannot be null");

        boolean skuAlreadyUsed = products.values()
            .stream()
            .anyMatch(existing ->
                existing.getSku().equals(product.getSku())
                && !existing.getId().equals(product.getId())
            );

        if (skuAlreadyUsed) {
            throw new IllegalArgumentException(
                "A product with this SKU already exists"
            );
        }

        products.put(product.getId(), product);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        if (sku == null || sku.isBlank()) {
            return Optional.empty();
        }

        String normalizedSku = sku.trim().toUpperCase(java.util.Locale.ROOT);

        return products.values()
            .stream()
            .filter(product -> product.getSku().equals(normalizedSku))
            .findFirst();
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    @Override
    public boolean existsBySku(String sku) {
        return findBySku(sku).isPresent();
    }

    @Override
    public boolean deleteById(UUID id) {
        return products.remove(id) != null;
    }
}
