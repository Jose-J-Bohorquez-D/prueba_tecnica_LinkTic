package com.linktic.test.inventory.service;

import com.linktic.test.inventory.client.ProductClient;
import com.linktic.test.inventory.entity.Inventory;
import com.linktic.test.inventory.entity.PurchaseTransaction;
import com.linktic.test.inventory.repository.InventoryRepository;
import com.linktic.test.inventory.repository.PurchaseTransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private PurchaseTransactionRepository transactionRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void purchase_Success() {
        UUID productId = UUID.randomUUID();
        String idempotencyKey = "key-success";
        int quantity = 5;

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setAvailable(10);

        when(productClient.validateProductExists(productId)).thenReturn(CompletableFuture.completedFuture(null));
        when(inventoryRepository.findById(productId)).thenReturn(Optional.of(inventory));

        inventoryService.purchase(productId, quantity, idempotencyKey);

        assertEquals(5, inventory.getAvailable());
        verify(inventoryRepository, times(1)).save(inventory);
        verify(transactionRepository, times(1)).saveAndFlush(any(PurchaseTransaction.class));
    }

    @Test
    void purchase_IdempotentRequest_ReturnsEarly() {
        UUID productId = UUID.randomUUID();
        String idempotencyKey = "key-idempotent";
        int quantity = 1;

        when(transactionRepository.saveAndFlush(any(PurchaseTransaction.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        inventoryService.purchase(productId, quantity, idempotencyKey);

        verify(productClient, never()).validateProductExists(any());
        verify(inventoryRepository, never()).findById(any());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void purchase_InsufficientStock_ThrowsException() {
        UUID productId = UUID.randomUUID();
        String idempotencyKey = "key-insufficient";
        int quantity = 15;

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setAvailable(10);

        when(productClient.validateProductExists(productId)).thenReturn(CompletableFuture.completedFuture(null));
        when(inventoryRepository.findById(productId)).thenReturn(Optional.of(inventory));

        assertThrows(IllegalArgumentException.class, () -> {
            inventoryService.purchase(productId, quantity, idempotencyKey);
        });

        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void purchase_ProductNotFound_ThrowsException() {
        UUID productId = UUID.randomUUID();
        String idempotencyKey = "key-not-found";
        int quantity = 1;

        when(productClient.validateProductExists(productId))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Product not found")));

        assertThrows(RuntimeException.class, () -> {
            inventoryService.purchase(productId, quantity, idempotencyKey);
        });

        verify(inventoryRepository, never()).findById(any());
    }

    @Test
    void purchase_ConcurrencyConflict_ThrowsException() {
        UUID productId = UUID.randomUUID();
        String idempotencyKey = "key-concurrency";
        int quantity = 5;

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setAvailable(10);

        when(productClient.validateProductExists(productId)).thenReturn(CompletableFuture.completedFuture(null));
        when(inventoryRepository.findById(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class)))
                .thenThrow(new ObjectOptimisticLockingFailureException("Conflict", null));

        assertThrows(IllegalStateException.class, () -> {
            inventoryService.purchase(productId, quantity, idempotencyKey);
        });
    }
}
