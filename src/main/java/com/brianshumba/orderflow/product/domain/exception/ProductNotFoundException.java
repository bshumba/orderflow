package com.brianshumba.orderflow.product.domain.exception;

import java.util.UUID;

public class ProductNotFoundException
    extends RuntimeException {
        public ProductNotFoundException(UUID id) {
            super("Product not found with ID: " + id);
        }
}
