// archivo: src/main/java/com/linktic/test/inventory/controller/InventoryController.java

package com.linktic.test.inventory.controller;

import com.linktic.test.inventory.entity.Inventory;
import com.linktic.test.inventory.service.InventoryService;
import com.linktic.test.inventory.dto.PurchaseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import com.linktic.test.inventory.dto.SingleResponse;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    
    @GetMapping("/{productId}")
    public SingleResponse<Inventory> get(@PathVariable UUID productId) {
        return new SingleResponse<>(service.getByProductId(productId));
    }

    
    @PostMapping
    public SingleResponse<Inventory> create(
            @RequestParam UUID productId,
            @RequestParam int quantity
    ) {
        return new SingleResponse<>(service.create(productId, quantity));
    }

    
    @PostMapping("/purchase")
    public void purchase(
            @Valid @RequestBody PurchaseRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    ) {
        service.purchase(
                request.getProductId(),
                request.getQuantity(),
                idempotencyKey
        );
    }
}