
package com.brianshumba.orderflow.product.application.service;

import com.brianshumba.orderflow.product.application.dto.CreateProductRequest;
import com.brianshumba.orderflow.product.application.dto.UpdateProductRequest;
import com.brianshumba.orderflow.product.domain.exception.DuplicateSkuException;
import com.brianshumba.orderflow.product.domain.exception.InvalidProductException;
import com.brianshumba.orderflow.product.domain.exception.ProductNotFoundException;
import com.brianshumba.orderflow.product.domain.model.Product;
import com.brianshumba.orderflow.product.domain.repository.ProductRepository;
import com.brianshumba.orderflow.product.infrastructure.persistence.InMemoryProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductService service;

    @BeforeEach
    void setUp() {
        ProductRepository repository = new InMemoryProductRepository();
        service = new ProductService(repository);
    }

    @Test
    void shouldCreateProductSuccessfully() {
        CreateProductRequest request = new CreateProductRequest(
            "LAP-001",
            "Dell Latitude",
            new BigDecimal("450.00")
        );

        Product product = service.createProduct(request);

        assertNotNull(product.getId());
        assertEquals("LAP-001", product.getSku());
        assertEquals("Dell Latitude", product.getName());
        assertEquals(new BigDecimal("450.00"), product.getPrice());
        assertTrue(product.isActive());
    }

    @Test
    void shouldRejectDuplicateSku() {
        CreateProductRequest request = new CreateProductRequest(
            "LAP-001",
            "Dell Latitude",
            new BigDecimal("450.00")
        );

        service.createProduct(request);

        assertThrows(
            DuplicateSkuException.class,
            () -> service.createProduct(request)
        );
    }

    @Test
    void shouldFindExistingProductById() {
        Product created = service.createProduct(
            new CreateProductRequest(
                "LAP-001",
                "Dell Latitude",
                new BigDecimal("450.00")
            )
        );

        Product found = service.getProductById(created.getId());

        assertEquals(created.getId(), found.getId());
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        UUID unknownId = UUID.randomUUID();

        assertThrows(
            ProductNotFoundException.class,
            () -> service.getProductById(unknownId)
        );
    }

    @Test
    void shouldReturnAllCreatedProducts() {
        service.createProduct(
            new CreateProductRequest(
                "LAP-001",
                "Dell Latitude",
                new BigDecimal("450.00")
            )
        );

        service.createProduct(
            new CreateProductRequest(
                "LAP-002",
                "HP Pavilion",
                new BigDecimal("550.00")
            )
        );

        service.createProduct(
            new CreateProductRequest(
                "LAP-003",
                "Asus ROG",
                new BigDecimal("850.00")
            )
        );

        assertEquals(3, service.getAllProducts().size());
    }

    @Test
    void shouldRejectBlankProductName() {
        CreateProductRequest request = new CreateProductRequest(
            "LAP-004",
            "",
            new BigDecimal("650.00")
        );

        assertThrows(
            InvalidProductException.class,
            () -> service.createProduct(request)
        );
    }

    @Test
    void shouldFindProductWithNormalizedSku() {
        CreateProductRequest request = new CreateProductRequest(
            "lap-005",
            "Lenovo ThinkPad",
            new BigDecimal("750.00")
        );

        Product createdProduct = service.createProduct(request);

        Product found = service.getProductById(createdProduct.getId());

        assertEquals("LAP-005", found.getSku());
    }

    @Test
    void shouldUpdateProductSuccessfully() {

        Product product = service.createProduct(
            new CreateProductRequest(
                "LAP-001",
                "Dell Latitude",
                new BigDecimal("450.00")
            )
        );

        UpdateProductRequest request = new UpdateProductRequest(
            "Dell Latitude 5420",
            new BigDecimal("500.00")
        );

        Product updated = service.updateProduct(product.getId(), request);

        assertEquals("Dell Latitude 5420", updated.getName());
        assertEquals(new BigDecimal("500.00"), updated.getPrice());

        assertEquals(product.getId(), updated.getId());
        assertEquals("LAP-001", updated.getSku());
    }

    @Test
    void shouldDeactivateProductSuccessfully() {

        Product product = service.createProduct(
            new CreateProductRequest(
                "LAP-001",
                "Dell Latitude",
                new BigDecimal("450.00")
            )
        );

        Product deactivated = service.deactivateProduct(product.getId());

        assertFalse(deactivated.isActive());
    }

    @Test
    void shouldRejectUpdateForUnknownProduct() {

        UUID unknownId = UUID.randomUUID();

        UpdateProductRequest request = new UpdateProductRequest(
            "Dell Latitude 5420",
            new BigDecimal("500.00")
        );

        assertThrows(
            ProductNotFoundException.class,
            () -> service.updateProduct(unknownId, request)
        );
    }

    @Test
    void shouldRejectInvalidPriceDuringUpdate() {

        CreateProductRequest createRequest = new CreateProductRequest(
            "LAP-001",
            "Dell Latitude",
            new BigDecimal("450.00")
        );

        Product product = service.createProduct(createRequest);

        UpdateProductRequest request = new UpdateProductRequest(
            "Dell Latitude 5420",
            new BigDecimal("-500.00")
        );

        assertThrows(
            InvalidProductException.class,
            () -> service.updateProduct(product.getId(), request)
        );
    }

    @Test
    void shouldPreserveOriginalDetailsWhenUpdateFails() {

        CreateProductRequest createRequest = new CreateProductRequest(
            "LAP-001",
            "Dell Latitude",
            new BigDecimal("450.00")
        );

        Product product = service.createProduct(createRequest);

        UpdateProductRequest updateRequest = new UpdateProductRequest(
            "Dell Latitude 5420",
            new BigDecimal("-500.00")
        );

        assertThrows(
            InvalidProductException.class,
            () -> service.updateProduct(product.getId(), updateRequest)
        );

        Product found = service.getProductById(product.getId());
        assertEquals("Dell Latitude", found.getName());
        assertEquals(new BigDecimal("450.00"), found.getPrice());
    }

    @Test
    void shouldRejectDeactivationForUnknownProduct() {

        UUID unknownId = UUID.randomUUID();

        assertThrows(
            ProductNotFoundException.class,
            () -> service.deactivateProduct(unknownId)
        );
    }

    @Test
    void shouldAllowDeactivatingProductTwice() {

        CreateProductRequest createRequest = new CreateProductRequest(
            "LAP-001",
            "Dell Latitude",
            new BigDecimal("450.00")
        );

        Product product = service.createProduct(createRequest);

        Product firstDeactivation = service.deactivateProduct(product.getId());
        assertFalse(firstDeactivation.isActive());

        Product secondDeactivation = service.deactivateProduct(product.getId());
        assertFalse(secondDeactivation.isActive());
    }
}
