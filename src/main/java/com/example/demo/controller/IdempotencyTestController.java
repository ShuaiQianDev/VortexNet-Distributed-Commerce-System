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
