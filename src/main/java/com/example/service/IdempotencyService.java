package com.example.demo.service;

import com.example.demo.entity.IdempotencyKey;
import com.example.demo.repository.IdempotencyKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Idempotency Service
 *
 * This service manages idempotent operations to prevent duplicate transactions.
 *
 * How it works:
 * 1. Client sends a request with a unique X-Idempotency-Key header
 * 2. Service checks if we've already processed this request
 * 3. If yes: return the cached response (idempotent!)
 * 4. If no: process the request and cache the response
 * 5. Next time the same key arrives, return cached response without reprocessing
 *
 * Storage strategy (dual-layer for performance):
 * - Redis: Fast cache (response in < 1ms)
 * - Database: Persistent storage (for reliability)
 *
 * Use cases:
 * - Payment processing (prevent duplicate charges)
 * - Order creation (prevent duplicate orders)
 * - Any critical financial operation
 */
@Service
@Slf4j
public class IdempotencyService {

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // Idempotency keys expire after 24 hours
    private static final long EXPIRY_SECONDS = 86400;

    /**
     * Check if this request has already been processed
     *
     * Returns the cached response if this idempotency key was already processed.
     * This is the key method for preventing duplicate transactions.
     *
     * Process:
     * 1. Try Redis first (fast, < 1ms)
     * 2. If not found, try database (slower, but more reliable)
     * 3. Return the cached response if found
     *
     * @param clientRequestId The unique idempotency key from client
     * @return Optional containing the cached response if found, empty otherwise
     */
    public Optional<String> getExistingResponse(String clientRequestId) {
        // Validate input
        if (clientRequestId == null || clientRequestId.isEmpty()) {
            return Optional.empty();
        }

        // Step 1: Check Redis cache (fast path)
        String cacheKey = "idempotency:" + clientRequestId;
        String cachedResponse = redisTemplate.opsForValue().get(cacheKey);

        if (cachedResponse != null) {
            log.info("Cache hit in Redis for idempotency key: {}", clientRequestId);
            return Optional.of(cachedResponse);
        }

        // Step 2: Check database (fallback)
        Optional<IdempotencyKey> dbKey = idempotencyKeyRepository
                .findByClientRequestId(clientRequestId);

        if (dbKey.isPresent() && dbKey.get().getIsProcessed()) {
            log.info("Cache hit in database for idempotency key: {}", clientRequestId);
            String response = dbKey.get().getResponse();

            // Repopulate Redis for future requests
            if (response != null) {
                redisTemplate.opsForValue()
                        .set(cacheKey, response, EXPIRY_SECONDS, TimeUnit.SECONDS);
            }

            return Optional.of(response);
        }

        log.info("No existing response found for idempotency key: {}", clientRequestId);
        return Optional.empty();
    }

    /**
     * Record a new request
     *
     * This method is called when we first encounter a new idempotency key.
     * It creates a record in both Redis (for fast access) and database (for durability).
     *
     * @param clientRequestId The unique idempotency key from client
     */
    public void recordRequest(String clientRequestId) {
        String key = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(1);  // 24 hours later

        // Save to database for persistence
        IdempotencyKey idempotencyKey = new IdempotencyKey(key, clientRequestId);
        idempotencyKey.setCreatedAt(now);
        idempotencyKey.setExpiresAt(expiresAt);
        idempotencyKey.setIsProcessed(false);

        idempotencyKeyRepository.save(idempotencyKey);

        // Save to Redis as a temporary marker
        // This prevents duplicate processing if the same key arrives during processing
        String cacheKey = "idempotency:" + clientRequestId;
        redisTemplate.opsForValue()
                .set(cacheKey, "", 300, TimeUnit.SECONDS);  // 5 minute marker

        log.info("Recorded new idempotency key in database and Redis: {}", clientRequestId);
    }

    /**
     * Record the response after successful processing
     *
     * This method is called after we've successfully processed the request.
     * It stores the response in both Redis (for speed) and database (for durability).
     *
     * @param clientRequestId The unique idempotency key from client
     * @param response The serialized response (JSON string)
     */
public void recordResponse(String clientRequestId, String response) {
        Optional<IdempotencyKey> idempotencyKey = idempotencyKeyRepository
                .findByClientRequestId(clientRequestId);

        if (idempotencyKey.isPresent()) {
            // Update database record
            IdempotencyKey key = idempotencyKey.get();
            key.setIsProcessed(true);
            key.setResponse(response);
            idempotencyKeyRepository.save(key);

            // Update Redis cache with the actual response
            String cacheKey = "idempotency:" + clientRequestId;
            redisTemplate.opsForValue()
                    .set(cacheKey, response, EXPIRY_SECONDS, TimeUnit.SECONDS);

            log.info("Recorded response for idempotency key: {}", clientRequestId);
        } else {
            log.warn("Failed to find idempotency key to record response: {}", clientRequestId);
        }
    }
}
