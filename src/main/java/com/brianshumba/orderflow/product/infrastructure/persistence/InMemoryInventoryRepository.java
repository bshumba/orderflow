
package com.brianshumba.orderflow.product.infrastructure.persistence;

import com.brianshumba.orderflow.product.domain.model.Inventory;
import com.brianshumba.orderflow.product.domain.repository.InventoryRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class InMemoryInventoryRepository implements InventoryRepository {

    private final Map<UUID, Inventory> inventories = new HashMap<>();

    @Override
    public void save(Inventory inventory) {
        Objects.requireNonNull(inventory, "Inventory cannot be null");

        inventories.put(
            inventory.getProductId(),
            inventory
        );
    }

    @Override
    public Optional<Inventory> findByProductId(UUID productId) {
        return Optional.ofNullable(inventories.get(productId));
    }
}
