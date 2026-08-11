package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private Long productId;
    private Integer availableQuantity;
    private Integer reservedQuantity;
    private Boolean isAvailable;
    private String message;
}
