package com.brianshumba.orderflow.product.domain.repository;

import com.brianshumba.orderflow.product.domain.model.Product;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface ProductRepository {

    void save(Product product);

    Optional<Product> findById(UUID id);

    Optional<Product> findBySku(String sku);

    List<Product> findAll();

    boolean existsBySku(String sku);

    boolean deleteById(UUID id);
}
