package com.linktic.test.inventory.service;

import com.linktic.test.inventory.entity.Inventory;
import com.linktic.test.inventory.entity.PurchaseTransaction;
import com.linktic.test.inventory.repository.InventoryRepository;
import com.linktic.test.inventory.repository.PurchaseTransactionRepository;
import com.linktic.test.inventory.client.ProductClient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.linktic.test.inventory.exception.DuplicateRequestException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private final InventoryRepository repository;
    private final PurchaseTransactionRepository transactionRepository;
    private final ProductClient productClient;

    public Inventory getByProductId(UUID productId) {
        return repository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Inventory no encontrado"));
    }

    public Inventory create(UUID productId, int quantity) {
        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setAvailable(quantity);
        inventory.setReserved(0);

        return repository.save(inventory);
    }

    @Transactional
    public void purchase(UUID productId, int quantity, String idempotencyKey) {
        // 1. Intentar registrar la transacción de inmediato.
        // Si falla por clave duplicada, la transacción se revierte y el método termina.
        try {
            PurchaseTransaction tx = new PurchaseTransaction();
            tx.setIdempotencyKey(idempotencyKey);
            tx.setProductId(productId);
            tx.setQuantity(quantity);
            transactionRepository.saveAndFlush(tx); // Usamos saveAndFlush para forzar la inserción inmediata
        } catch (DataIntegrityViolationException e) {
            // Esto ocurre si la Idempotency-Key ya existe. Es una petición duplicada.
            log.warn("Duplicate purchase request detected with Idempotency-Key: {}", idempotencyKey);
            throw new DuplicateRequestException("Duplicate request");
        }

        // 2. Validación contra products-service (con resiliencia)
        try {
            productClient.validateProductExists(productId).join(); // Bloqueamos hasta que el futuro se complete
        } catch (Exception e) {
            Throwable cause = e;
            while (cause.getCause() != null && (cause instanceof CompletionException || cause instanceof ExecutionException)) {
                cause = cause.getCause();
            }
            if (cause instanceof ResponseStatusException rse) {
                throw rse;
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Product service is currently unavailable");
        }

        try {
            Inventory inventory = repository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));

            if (inventory.getAvailable() < quantity) {
                throw new IllegalArgumentException("Insufficient stock for product: " + productId);
            }

            // 3. Descontar stock (la anotación @Version se encargará del bloqueo optimista)
            inventory.setAvailable(inventory.getAvailable() - quantity);
            repository.save(inventory);

            // Evento estructurado
            log.info("EVENT: InventoryChanged | ProductId: {} | Quantity: -{} | Remaining: {}",
                productId, quantity, inventory.getAvailable());

        } catch (ObjectOptimisticLockingFailureException e) {
            log.warn("Optimistic locking conflict for product: {}. Rolling back purchase.", productId);
            // La transacción se revertirá automáticamente gracias a la excepción, eliminando la PurchaseTransaction.
            throw new IllegalStateException("Concurrency conflict, please try again.");
        }
    }
}
