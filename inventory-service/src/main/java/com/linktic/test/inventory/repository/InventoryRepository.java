// archivo: src/main/java/com/linktic/test/inventory/repository/InventoryRepository.java
package com.linktic.test.inventory.repository;

import com.linktic.test.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
}

