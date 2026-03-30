// archivo: package com.linktic.test.products.repository;
package com.linktic.test.products.repository;

import com.linktic.test.products.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepository extends JpaRepository<Product, UUID> {

    Optional<Product> findBySku(String sku);

    Page<Product> findByStatus(String status, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseOrSkuContainingIgnoreCase(String name, String sku, Pageable pageable);

    Page<Product> findByStatusAndNameContainingIgnoreCaseOrStatusAndSkuContainingIgnoreCase(
            String status1, String name, String status2, String sku, Pageable pageable
    );
}

