// archivo: src/main/java/com/linktic/test/products/service/ProductService.java
package com.linktic.test.products.service;

import com.linktic.test.products.dto.ProductRequest;
import com.linktic.test.products.dto.ProductResponse;
import com.linktic.test.products.entity.Product;
import com.linktic.test.products.exception.BusinessException;
import com.linktic.test.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.*;
import com.linktic.test.products.dto.PagedResponse;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repository;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "price");

    public ProductResponse create(ProductRequest request) {

        repository.findBySku(request.getSku()).ifPresent(p -> {
            throw new BusinessException("SKU ya existe");
        });

        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStatus(request.getStatus());

        Product saved = repository.save(product);

        return toResponse(saved);
    }

    public PagedResponse<ProductResponse> getAll(
        int page,
        int size,
        String status,
        String search,
        String sortBy
    ) {

        Pageable pageable = PageRequest.of(
            page,
            size,
            buildSort(sortBy)
        );

        Page<Product> result;

        if (status != null && search != null) {
            result = repository.findByStatusAndNameContainingIgnoreCaseOrStatusAndSkuContainingIgnoreCase(
                status, search, status, search, pageable
            );
        } else if (status != null) {
            result = repository.findByStatus(status, pageable);
        } else if (search != null) {
            result = repository.findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(
                search, search, pageable
            );
        } else {
            result = repository.findAll(pageable);
        }

        List<ProductResponse> data = result.getContent()
            .stream()
            .map(this::toResponse)
            .toList();

        return new PagedResponse<>(
            data,
            new PagedResponse.Meta(
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
            )
        );
    }

    public ProductResponse getById(UUID id) {

        Product product = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Producto no encontrado"
            ));

        return toResponse(product);
    }

    public ProductResponse update(UUID id, ProductRequest request) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));
        if (!product.getSku().equals(request.getSku())) {
            repository.findBySku(request.getSku()).ifPresent(p -> {
                throw new BusinessException("SKU ya existe");
            });
        }
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStatus(request.getStatus());
        Product saved = repository.save(product);
        return toResponse(saved);
    }

    public void delete(UUID id) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                ));
        repository.delete(product);
    }

    
    private ProductResponse toResponse(Product p) {
        ProductResponse r = new ProductResponse();
        r.setId(p.getId());
        r.setSku(p.getSku());
        r.setName(p.getName());
        r.setPrice(p.getPrice());
        r.setStatus(p.getStatus());
        return r;
    }

    private Sort buildSort(String sortBy) {
        String field = "createdAt";
        Sort.Direction direction = Sort.Direction.DESC;

        if (sortBy != null && !sortBy.isBlank()) {
            String[] parts = sortBy.split(",", 2);
            String requestedField = parts[0].trim();

            if (!ALLOWED_SORT_FIELDS.contains(requestedField)) {
                throw new ResponseStatusException(
                        HttpStatus.UNPROCESSABLE_ENTITY,
                        "sortBy inválido. Campos permitidos: createdAt, price"
                );
            }

            field = requestedField;

            if (parts.length == 2) {
                String requestedDirection = parts[1].trim().toLowerCase();
                if ("asc".equals(requestedDirection)) {
                    direction = Sort.Direction.ASC;
                } else if ("desc".equals(requestedDirection)) {
                    direction = Sort.Direction.DESC;
                } else {
                    throw new ResponseStatusException(
                            HttpStatus.UNPROCESSABLE_ENTITY,
                            "sortBy inválido. Dirección permitida: asc o desc"
                    );
                }
            }
        }

        return Sort.by(direction, field);
    }
}
