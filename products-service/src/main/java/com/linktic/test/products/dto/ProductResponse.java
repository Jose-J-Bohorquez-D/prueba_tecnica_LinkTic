// archivo: src/main/java/com/linktic/test/products/dto/ProductResponse.java
package com.linktic.test.products.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ProductResponse {

    private UUID id;
    private String sku;
    private String name;
    private Double price;
    private String status;
}