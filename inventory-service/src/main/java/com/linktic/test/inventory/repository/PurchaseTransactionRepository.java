//archivo:inventory-service/src/main/java/com/linktic/test/inventory/repository/PurchaseTransactionRepository.java
package com.linktic.test.inventory.repository;

import com.linktic.test.inventory.entity.PurchaseTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransaction, String> {
}