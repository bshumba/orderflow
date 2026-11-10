
package com.brianshumba.orderflow.product.application.service;

import com.brianshumba.orderflow.product.application.dto.CreateProductRequest;
import com.brianshumba.orderflow.product.application.dto.UpdateProductRequest;
import com.brianshumba.orderflow.product.domain.exception.DuplicateSkuException;
import com.brianshumba.orderflow.product.domain.exception.ProductNotFoundException;
import com.brianshumba.orderflow.product.domain.model.Product;
import com.brianshumba.orderflow.product.domain.repository.ProductRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceMockTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    @Test
    void shouldNotSaveProductWhenSkuAlreadyExists() {

        CreateProductRequest request = new CreateProductRequest(
            "LAP-001",
            "Dell Latitude",
            new BigDecimal("450.00")
        );

        when(repository.existsBySku("LAP-001"))
            .thenReturn(true);

        assertThrows(
            DuplicateSkuException.class,
            () -> service.createProduct(request)
        );

        verify(repository, never())
            .save(any(Product.class));
    }

    @Test
    void shouldSaveProductWithCorrectDetails() {

        CreateProductRequest request = new CreateProductRequest(
            "lap-001",
            "Dell Latitude",
            new BigDecimal("450.00")
        );

        service.createProduct(request);

        ArgumentCaptor<Product> captor =
            ArgumentCaptor.forClass(Product.class);

        verify(repository).save(captor.capture());

        Product savedProduct = captor.getValue();

        assertEquals("LAP-001", savedProduct.getSku());
        assertEquals("Dell Latitude", savedProduct.getName());
        assertEquals(
            new BigDecimal("450.00"),
            savedProduct.getPrice()
        );
    }

    @Test
    void shouldFindProductById() {
        CreateProductRequest request = new CreateProductRequest(
            "lap-001",
            "Dell Latitude",
            new BigDecimal("450.00")
        );

        Product product = service.createProduct(request);

        when(repository.findById(product.getId()))
            .thenReturn(Optional.of(new Product(
                product.getId(),
                "LAP-001",
                "Dell Latitude",
                new BigDecimal("450.00")
            )));

        Product foundProduct = service.getProductById(product.getId());
        assertEquals(product.getId(), foundProduct.getId());
    }

    @Test
    void shouldThrowWhenProductDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id))
            .thenReturn(Optional.empty());

        assertThrows(
            ProductNotFoundException.class,
            () -> service.getProductById(id)
        );
    }

    @Test
    void shouldSaveUpdatedProduct() {

        Product product = new Product(
            UUID.randomUUID(),
            "LAP-001",
            "Dell Latitude",
            new BigDecimal("450.00")
        );

        when(repository.findById(product.getId()))
            .thenReturn(Optional.of(product));

        UpdateProductRequest request = new UpdateProductRequest(
            "Dell Latitude Updated",
            new BigDecimal("500.00")
        );

        Product updated = service.updateProduct(
            product.getId(),
            request
        );

        assertEquals("Dell Latitude Updated", updated.getName());
        assertEquals(new BigDecimal("500.00"), updated.getPrice());

        verify(repository).save(product);
    }

    @Test
    void shouldDeactivateExistingProduct() {

        Product product = new Product(
            UUID.randomUUID(),
            "LAP-001",
            "Dell Latitude",
            new BigDecimal("450.00")
        );

        when(repository.findById(product.getId()))
            .thenReturn(Optional.of(product));

        Product deactivated = service.deactivateProduct(
            product.getId()
        );

        assertFalse(deactivated.isActive());

        verify(repository).save(product);
    }
}
