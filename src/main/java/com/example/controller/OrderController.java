package com.example.demo.controller;

import com.example.demo.dto.OrderRequest;
import com.example.demo.dto.OrderResponse;
import com.example.demo.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        long startTime = System.currentTimeMillis();

        log.info("Order request received: user={}, product={}, quantity={}",
                request.getUserId(), request.getProductId(), request.getQuantity());

        OrderResponse response = orderService.createOrder(request);

        long duration = System.currentTimeMillis() - startTime;
        log.info("Order processing completed in {}ms, status: {}",
                duration, response.getStatus());

        return ResponseEntity.ok(response);
    }
}
