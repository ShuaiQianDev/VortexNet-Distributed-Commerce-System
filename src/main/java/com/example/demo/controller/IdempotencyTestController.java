package com.example.demo.controller;

import com.example.demo.service.IdempotencyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Idempotency Test Controller
 * 
 * This controller demonstrates idempotent payment processing.
 * It shows how to prevent duplicate charges even when clients retry requests.
 * 
 * API Endpoint: POST /api/idempotency-test/payment
 * 
 * Required Header: X-Idempotency-Key (unique identifier for this request)
 * 
 * Key behaviors:
 * - First request with key X: Process payment, return 201 Created
 * - Second request with same key X: Return cached response, return 200 OK
 * - Third request with different key Y: Process payment, return 201 Created
 * 
 * This guarantees idempotency: calling multiple times = calling once
 */
@RestController
@RequestMapping("/api/idempotency-test")
@Slf4j
    public class IdempotencyTestController {
    
    @Autowired
    private IdempotencyService idempotencyService;
    
    /**
     * Test payment endpoint with idempotency support
     * 
     * This endpoint demonstrates how to safely handle payment retries
     * by using idempotency keys.
     * 
     * Request Headers:
     * - X-Idempotency-Key (required): A unique identifier for this payment
     * 
     * Request Body Example:
     * {
     *   "amount": 99.99,
     *   "userId": "user123"
     * }
     * 
     * Response for first request:
     * - Status: 201 Created
     * - Body includes transactionId (newly created)
     * 
     * Response for retry with same key:
     * - Status: 200 OK
     * - Body includes same transactionId (from cache)
     * 
     * @param idempotencyKey The unique identifier for this payment
     * @param request The payment request details
     * @return Response with transaction details
     */
    @PostMapping("/payment")
    public ResponseEntity<?> testPayment(
        @RequestHeader(value = "X-Idempotency-Key", required = true) 
        String idempotencyKey,
        @RequestBody PaymentTestRequest request) {
        
        log.info("Processing payment with idempotency key: {}", idempotencyKey);
        log.info("Amount: {}, User: {}", request.getAmount(), request.getUserId());
        
        try {
            // ===== Step 1: Check if this request was already processed =====
            Optional<String> existingResponse = idempotencyService.getExistingResponse(idempotencyKey);
            
            if (existingResponse.isPresent()) {
                log.info("Cache hit! Returning existing response for key: {}", idempotencyKey);
                
                // This is a retry - return cached response
                // This prevents duplicate charge!
                return ResponseEntity
                    .status(HttpStatus.OK)  // 200 OK, not 201
                    .body(existingResponse.get());
            }
            // ===== Step 2: Record that we're processing this request =====
            idempotencyService.recordRequest(idempotencyKey);
            log.info("Recorded new request for key: {}", idempotencyKey);
            
            // ===== Step 3: Process the payment (simulate) =====
            log.info("Processing payment...");
            Thread.sleep(500);  // Simulate processing time
            
            // Generate response with transaction details
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("transactionId", UUID.randomUUID().toString());
            response.put("amount", request.getAmount());
            response.put("userId", request.getUserId());
            response.put("timestamp", System.currentTimeMillis());
            response.put("idempotencyKey", idempotencyKey);
            response.put("message", "Payment processed successfully");
            
            // Convert response to JSON string for caching
            String jsonResponse = new ObjectMapper().writeValueAsString(response);
            
            // ===== Step 4: Store response for future retries =====
            idempotencyService.recordResponse(idempotencyKey, jsonResponse);
            log.info("Payment processed and response cached for key: {}", idempotencyKey);
            
            // Return 201 Created for new payment
            return ResponseEntity
                .status(HttpStatus.CREATED)  // 201 Created
                .body(response);
                
        } catch (InterruptedException e) {
            log.error("Payment processing interrupted", e);
            Thread.currentThread().interrupt();
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Payment processing interrupted: " + e.getMessage());
                
        } catch (Exception e) {
            log.error("Payment processing failed", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Payment failed: " + e.getMessage());
        }
    }
     /**
     * Health check endpoint
     * 
     * Simple endpoint to verify the application is running
     * 
     * @return Status message
     */
        @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "idempotency-test");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Payment Test Request DTO
     * 
     * Contains payment information
     */
        public static class PaymentTestRequest {
        
        private Double amount;
        private String userId;
        
        // Constructors
        public PaymentTestRequest() {
        }
        
        public PaymentTestRequest(Double amount, String userId) {
            this.amount = amount;
            this.userId = userId;
        }
        
        // Getters and Setters
        public Double getAmount() {
            return amount;
        }
        
        public void setAmount(Double amount) {
            this.amount = amount;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public void setUserId(String userId) {
            this.userId = userId;
        }
        
        @Override
        public String toString() {
            return "PaymentTestRequest{" +
                    "amount=" + amount +
                    ", userId='" + userId + '\'' +
                    '}';
        }
    }
}
