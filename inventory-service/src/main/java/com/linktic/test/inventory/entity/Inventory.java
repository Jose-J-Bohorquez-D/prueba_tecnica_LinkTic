//archivo: inventory-service/src/main/jave/com/linktic/test/inventory/entity/Inventory.java
package com.linktic.test.inventory.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "inventory")
@Data
public class Inventory {

    @Id
    private UUID productId;

    private Integer available;

    private Integer reserved;

    @Version
    private Integer version;
}