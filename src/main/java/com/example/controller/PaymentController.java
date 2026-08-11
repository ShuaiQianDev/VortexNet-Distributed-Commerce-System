package com.example.demo.controller;

import com.example.demo.dto.PaymentRequest;
import com.example.demo.dto.PaymentResponse;
import com.example.demo.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payment")
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("GET /api/payment/health - Health check");
        return ResponseEntity.ok("Payment service is healthy");
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @RequestBody PaymentRequest request) {

        long startTime = System.currentTimeMillis();
        log.info("Payment request received for user: {}, amount: {}",
                request.getUserId(), request.getAmount());

        PaymentResponse response = paymentService.processPayment(request);

        long duration = System.currentTimeMillis() - startTime;
        log.info("Payment processed in {}ms, status: {}",
                duration, response.getStatus());

        return ResponseEntity.ok(response);
    }
}
