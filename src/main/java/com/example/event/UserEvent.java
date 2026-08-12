package com.example.demo.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eventId;
    private String userId;
    private EventType eventType;
    private Long productId;
    private Double amount;
    private Long timestamp;
    private String metadata;
}
