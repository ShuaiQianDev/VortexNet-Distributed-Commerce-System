package com.example.demo.llm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String model;
    private List<Message> messages;
    private Integer max_tokens;
    private Double temperature;
    private Double top_p;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
