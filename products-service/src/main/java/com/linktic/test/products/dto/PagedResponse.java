// archivo: src/main/java/com/linktic/test/products/dto/PagedResponse.java
package com.linktic.test.products.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class PagedResponse<T> {

    private List<T> data;
    private Meta meta;

    @Data
    @AllArgsConstructor
    public static class Meta {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
    }
}