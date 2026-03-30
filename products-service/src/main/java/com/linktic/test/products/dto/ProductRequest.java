// archivo: src/main/java/com/linktic/test/products/dto/ProductRequest.java
package com.linktic.test.products.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank(message = "SKU es obligatorio")
    private String sku;

    @NotBlank(message = "Nombre es obligatorio")
    private String name;

    @NotNull(message = "Precio es obligatorio")
    @PositiveOrZero(message = "Precio debe ser mayor o igual a 0")
    private Double price;

    @NotBlank(message = "Estado es obligatorio")
    private String status;
}