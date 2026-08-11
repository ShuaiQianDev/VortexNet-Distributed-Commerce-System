package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderId;
    private String userId;
    private Long productId;
    private Integer quantity;
    private Double totalPrice;
    private String status;
    private String paymentStatus;
    private String transactionId;
    private Long timestamp;
}
