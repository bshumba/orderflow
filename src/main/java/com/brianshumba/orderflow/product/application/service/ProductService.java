package com.brianshumba.orderflow.product.application.service;

import com.brianshumba.orderflow.product.application.dto.CreateProductRequest;
import com.brianshumba.orderflow.product.domain.exception.DuplicateSkuException;
import com.brianshumba.orderflow.product.domain.exception.ProductNotFoundException;
import com.brianshumba.orderflow.product.application.dto.UpdateProductRequest;
import com.brianshumba.orderflow.product.domain.model.Product;
import com.brianshumba.orderflow.product.domain.repository.ProductRepository;

import java.util.Objects;
import java.util.UUID;
import java.util.List;

public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = Objects.requireNonNull(
            repository,
            "ProductRepository cannot be null");
    }

    public Product createProduct(CreateProductRequest request) {
        Objects.requireNonNull(request, "CreateProductRequest cannot be null");

        Product product = new Product(
            UUID.randomUUID(),
            request.sku(),
            request.name(),
            request.price()
        );

        if (repository.existsBySku(product.getSku())) {
            throw new DuplicateSkuException(product.getSku());
        }

        repository.save(product);

        return product;
    }

    public Product getProductById(UUID id) {
        return repository.findById(id)
            .orElseThrow(
                () -> new ProductNotFoundException(id)
            );
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    public Product updateProduct(UUID id, UpdateProductRequest request) {

        Objects.requireNonNull(request, "UpdateProductRequest cannot be null");

        Product product = getProductById(id);

        product.updateDetails(
            request.name(),
            request.price()
        );

        repository.save(product);

        return product;
    }

    public Product deactivateProduct(UUID id) {

        Product product = getProductById(id);

        product.deactivate();

        repository.save(product);

        return product;
    }

}
