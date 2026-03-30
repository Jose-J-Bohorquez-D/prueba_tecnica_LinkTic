//archivo:inventory-service/src/main/java/com/linktic/test/inventory/entity/PurchaseTransaction.java
package com.linktic.test.inventory.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.domain.Persistable;

import java.util.UUID;

@Entity
@Table(name = "purchase_transaction")
@Data
public class PurchaseTransaction implements Persistable<String> {

    @Id
    private String idempotencyKey;

    private UUID productId;

    private Integer quantity;

    @Transient
    private boolean isNew = true;

    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNew = false;
    }

    @Override
    public String getId() {
        return idempotencyKey;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }
}
