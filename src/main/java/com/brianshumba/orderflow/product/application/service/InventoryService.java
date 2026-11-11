
package com.brianshumba.orderflow.product.application.service;

import com.brianshumba.orderflow.product.domain.exception.ProductNotFoundException;
import com.brianshumba.orderflow.product.domain.model.Inventory;
import com.brianshumba.orderflow.product.domain.repository.InventoryRepository;
import com.brianshumba.orderflow.product.domain.repository.ProductRepository;

import java.util.Objects;
import java.util.UUID;

public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;

    public InventoryService(
        InventoryRepository inventoryRepository,
        ProductRepository productRepository
    ) {
        this.inventoryRepository = Objects.requireNonNull(inventoryRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
    }

    public Inventory receiveStock(UUID productId, int quantity) {

        ensureProductExists(productId);

        Inventory inventory = inventoryRepository
            .findByProductId(productId)
            .orElseGet(() -> new Inventory(productId));

        inventory.receiveStock(quantity);

        inventoryRepository.save(inventory);

        return inventory;
    }

    public Inventory getInventory(UUID productId) {

        ensureProductExists(productId);

        return inventoryRepository
            .findByProductId(productId)
            .orElseGet(() -> new Inventory(productId));
    }

    public Inventory reserveStock(UUID productId, int quantity) {

        Inventory inventory = getInventory(productId);

        inventory.reserveStock(quantity);

        inventoryRepository.save(inventory);

        return inventory;
    }

    public Inventory releaseReservation(UUID productId, int quantity) {

        Inventory inventory = getInventory(productId);

        inventory.releaseReservation(quantity);

        inventoryRepository.save(inventory);

        return inventory;
    }

    public Inventory fulfillReservation(UUID productId, int quantity) {

        Inventory inventory = getInventory(productId);

        inventory.fulfillReservation(quantity);

        inventoryRepository.save(inventory);

        return inventory;
    }

    public Inventory adjustStock(UUID productId, int newQuantity) {

        Inventory inventory = getInventory(productId);

        inventory.adjustStock(newQuantity);

        inventoryRepository.save(inventory);

        return inventory;
    }

    private void ensureProductExists(UUID productId) {

        if (productRepository.findById(productId).isEmpty()) {
            throw new ProductNotFoundException(productId);
        }
    }
}
