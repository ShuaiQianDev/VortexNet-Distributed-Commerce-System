package com.example.demo.service;

import com.example.demo.dto.PaymentRequest;
import com.example.demo.dto.PaymentResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PaymentService {

    @CircuitBreaker(
            name = "paymentService",
            fallbackMethod = "paymentFallback"
    )
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for user: {}, amount: {}",
                request.getUserId(), request.getAmount());

        simulatePaymentProcessing();

        PaymentResponse response = new PaymentResponse(
                "success",
                "Payment processed successfully",
                generateTransactionId(),
                System.currentTimeMillis()
        );

        log.info("Payment processed successfully, transactionId: {}",
                response.getTransactionId());

        return response;
    }

    public PaymentResponse paymentFallback(
            PaymentRequest request,
            Exception e) {

        log.error("Payment service circuit breaker triggered. Reason: {}",
                e.getMessage());

        return new PaymentResponse(
                "circuit_breaker_open",
                "Payment service is temporarily unavailable. Please try again later.",
                null,
                System.currentTimeMillis()
        );
    }

    private void simulatePaymentProcessing() {
        if (Math.random() < 0.1) {
            throw new RuntimeException(
                    "Payment processing failed: Database connection error");
        }

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Payment processing interrupted", e);
        }
    }

    private String generateTransactionId() {
        return "txn_" + System.nanoTime();
    }
}
