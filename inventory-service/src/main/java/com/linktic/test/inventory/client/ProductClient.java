package com.linktic.test.inventory.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class ProductClient {

    private final WebClient webClient;

    public ProductClient(WebClient webClient) {
        this.webClient = webClient;
    }

    @TimeLimiter(name = "productsService", fallbackMethod = "fallbackProduct")
    @CircuitBreaker(name = "productsService") // No necesita fallback aquí, TimeLimiter lo maneja
    @Retry(name = "productsService")
    public CompletableFuture<Void> validateProductExists(UUID productId) {
        return webClient.get()
                .uri("/products/{id}", productId)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found: " + productId)))
                .onStatus(status -> status.is5xxServerError(),
                        response -> Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Products service is unavailable")))
                .toBodilessEntity()
                .then()
                .toFuture();
    }

    private CompletableFuture<Void> fallbackProduct(UUID productId, Throwable t) {
        // Log the error for observability
        System.out.println("Fallback triggered for product validation: " + productId + ", error: " + t.getMessage());

        // Propagate a clear error to the caller
        return CompletableFuture.failedFuture(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Product service is currently unavailable. Please try again later."));
    }
}