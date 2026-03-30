package com.linktic.test.products.service;

import com.linktic.test.products.dto.PagedResponse;
import com.linktic.test.products.dto.ProductRequest;
import com.linktic.test.products.dto.ProductResponse;
import com.linktic.test.products.entity.Product;
import com.linktic.test.products.exception.BusinessException;
import com.linktic.test.products.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    @Test
    void createProduct_Success() {
        ProductRequest request = new ProductRequest();
        request.setSku("SKU123");
        request.setName("Product 1");
        request.setPrice(100.0);
        request.setStatus("ACTIVE");

        when(repository.findBySku("SKU123")).thenReturn(Optional.empty());

        Product savedProduct = new Product();
        savedProduct.setId(UUID.randomUUID());
        savedProduct.setSku("SKU123");

        when(repository.save(any(Product.class))).thenReturn(savedProduct);

        ProductResponse response = service.create(request);

        assertNotNull(response);
        assertEquals("SKU123", response.getSku());
        verify(repository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_DuplicateSku_ThrowsException() {
        ProductRequest request = new ProductRequest();
        request.setSku("SKU123");

        when(repository.findBySku("SKU123")).thenReturn(Optional.of(new Product()));

        assertThrows(BusinessException.class, () -> service.create(request));
        verify(repository, never()).save(any(Product.class));
    }

    @Test
    void getById_Success() {
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setId(productId);
        product.setSku("SKU123");

        when(repository.findById(productId)).thenReturn(Optional.of(product));

        ProductResponse response = service.getById(productId);

        assertNotNull(response);
        assertEquals("SKU123", response.getSku());
    }

    @Test
    void getById_NotFound_ThrowsException() {
        UUID productId = UUID.randomUUID();
        when(repository.findById(productId)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> service.getById(productId));
    }

    @Test
    void getAll_Success() {
        Page<Product> page = new PageImpl<>(Collections.singletonList(new Product()));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResponse<ProductResponse> response = service.getAll(0, 10, null, null, null);

        assertNotNull(response);
        assertEquals(1, response.getData().size());
        assertEquals(1, response.getMeta().getTotalElements());
    }

    @Test
    void getAll_WithSortByFieldAndDirection_DoesNotThrow() {
        Page<Product> page = new PageImpl<>(Collections.singletonList(new Product()));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        assertDoesNotThrow(() -> service.getAll(0, 10, null, null, "createdAt,desc"));
    }

    @Test
    void getAll_WithInvalidSortBy_ThrowsUnprocessableEntity() {
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> service.getAll(0, 10, null, null, "createdAt,desc,extra")
        );
        assertEquals(422, ex.getStatusCode().value());
    }
}
