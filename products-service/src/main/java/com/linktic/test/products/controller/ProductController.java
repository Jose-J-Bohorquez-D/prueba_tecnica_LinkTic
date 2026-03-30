// archivo: src/main/java/com/linktic/test/products/controller/ProductController.java
package com.linktic.test.products.controller;

import com.linktic.test.products.service.ProductService;
import com.linktic.test.products.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping({"/products", "/api/internal/products"})
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    @PostMapping
    public SingleResponse<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return new SingleResponse<>(service.create(request));
    }

    @GetMapping
    public PagedResponse<ProductResponse> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String sortBy
    ) {
        return service.getAll(page, size, status, search, sortBy);
    }

    @GetMapping("/{id}")
    public SingleResponse<ProductResponse> getById(@PathVariable UUID id) {
        return new SingleResponse<>(service.getById(id));
    }

    @PutMapping("/{id}")
    public SingleResponse<ProductResponse> update(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return new SingleResponse<>(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public SingleResponse<String> delete(@PathVariable UUID id) {
        service.delete(id);
        return new SingleResponse<>("deleted");
    }


}
