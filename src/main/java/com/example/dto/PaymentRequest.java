package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private Double amount;
    private String currency;
    private String description;
}
