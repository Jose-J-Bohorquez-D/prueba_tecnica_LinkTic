//archivo: src/main/java/com/linktic/test/inventory/dto/PurchaseRequest.java
package com.linktic.test.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.UUID;

@Data
public class PurchaseRequest {

    @NotNull(message = "El ID del producto es obligatorio")
    private UUID productId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    private Integer quantity;
}