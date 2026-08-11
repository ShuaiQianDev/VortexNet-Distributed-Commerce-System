package com.example.demo.service;

import com.example.demo.dto.OrderRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.dto.PaymentRequest;
import com.example.demo.dto.PaymentResponse;
import com.example.demo.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderService {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private ProductService productService;

    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order for user: {}, product: {}, quantity: {}",
                request.getUserId(), request.getProductId(), request.getQuantity());

        // Step 1: Check inventory
        Integer stock = productService.getProductStock(request.getProductId());
        if (stock == null || stock < request.getQuantity()) {
            log.error("Insufficient inventory for product: {}", request.getProductId());
            return new OrderResponse(
                    null, request.getUserId(), request.getProductId(),
                    request.getQuantity(), 0.0, "CANCELLED",
                    "INVENTORY_ERROR", null, System.currentTimeMillis()
            );
        }

        // Step 2: Get product price
        Double price = productService.getProductPrice(request.getProductId());
        if (price == null) {
            price = 0.0;
        }
        Double totalPrice = price * request.getQuantity();
        String orderId = "ORD_" + System.nanoTime();

        // Step 3: Process payment (with circuit breaker from Part 1.3)
        PaymentRequest paymentRequest = new PaymentRequest(
                request.getUserId(),
                totalPrice,
                "USD",
                "Order: " + orderId
        );

        PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);

        // Step 4: Return result
        if ("success".equals(paymentResponse.getStatus())) {
            log.info("Order created and paid: {}", orderId);
            return new OrderResponse(
                    orderId,
                    request.getUserId(),
                    request.getProductId(),
                    request.getQuantity(),
                    totalPrice,
                    "PAID",
                    "SUCCESS",
                    paymentResponse.getTransactionId(),
                    System.currentTimeMillis()
            );
        } else if ("circuit_breaker_open".equals(paymentResponse.getStatus())) {
            log.error("Payment service circuit breaker open for order: {}", orderId);
            return new OrderResponse(
                    orderId,
                    request.getUserId(),
                    request.getProductId(),
                    request.getQuantity(),
                    totalPrice,
                    "PENDING",
                    "CIRCUIT_BREAKER_OPEN",
                    null,
                    System.currentTimeMillis()
            );
        } else {
            log.error("Payment failed for order: {}", orderId);
            return new OrderResponse(
                    orderId,
                    request.getUserId(),
                    request.getProductId(),
                    request.getQuantity(),
                    totalPrice,
                    "CANCELLED",
                    "PAYMENT_FAILED",
                    null,
                    System.currentTimeMillis()
            );
        }
    }
}
