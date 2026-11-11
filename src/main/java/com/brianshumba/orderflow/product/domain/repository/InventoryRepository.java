
package com.brianshumba.orderflow.product.domain.repository;

import com.brianshumba.orderflow.product.domain.model.Inventory;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository {

    void save(Inventory inventory);

    Optional<Inventory> findByProductId(UUID productId);
}
