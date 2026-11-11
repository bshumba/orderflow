package com.brianshumba.orderflow;

import java.math.BigDecimal;

import com.brianshumba.orderflow.product.application.dto.CreateProductRequest;
import com.brianshumba.orderflow.product.application.service.InventoryService;
import com.brianshumba.orderflow.product.application.service.ProductService;
import com.brianshumba.orderflow.product.domain.exception.InsufficientStockException;
import com.brianshumba.orderflow.product.domain.exception.InvalidStockOperationException;
import com.brianshumba.orderflow.product.domain.exception.ProductNotFoundException;
import com.brianshumba.orderflow.product.domain.repository.InventoryRepository;
import com.brianshumba.orderflow.product.domain.repository.ProductRepository;
import com.brianshumba.orderflow.product.infrastructure.persistence.InMemoryInventoryRepository;
import com.brianshumba.orderflow.product.infrastructure.persistence.InMemoryProductRepository;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {

        ProductRepository productRepository =
            new InMemoryProductRepository();

        InventoryRepository inventoryRepository =
            new InMemoryInventoryRepository();

        ProductService productService =
            new ProductService(productRepository);

        InventoryService inventoryService =
            new InventoryService(
                inventoryRepository,
                productRepository
            );

        try {
            CreateProductRequest request = new CreateProductRequest(
                "LAP-001",
                "Dell Latitude",
                new BigDecimal("550.00")
            );
            var product = productService.createProduct(request);

            System.out.println("Product created: " + product);

            inventoryService.receiveStock(product.getId(), 50);
            System.out.println("Inventory updated: " +  inventoryService.getInventory(product.getId()));

            inventoryService.reserveStock(product.getId(), 8);
            System.out.println("Stock reserved: " + inventoryService.getInventory(product.getId()));
        } catch (ProductNotFoundException e) {
            System.err.println("Product not found: " + e.getMessage());
        } catch (InsufficientStockException e) {
            System.err.println("Insufficient stock: " + e.getMessage());
        } catch (InvalidStockOperationException e) {
            System.err.println("Invalid stock operation: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
        }
    }
}
