package com.example.demo.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import java.time.Duration;

@Configuration
@Slf4j
public class OpenAIConfig {

    @Value("${openai.api.key:demo-key}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${openai.api.timeout:30}")
    private Integer timeout;

    @Bean
    public RestTemplate openaiRestTemplate(RestTemplateBuilder builder) {
        log.info("Initializing OpenAI RestTemplate with timeout: {}s", timeout);

        return builder
                .setConnectTimeout(Duration.ofSeconds(timeout))
                .setReadTimeout(Duration.ofSeconds(timeout))
                .interceptors((request, body, execution) -> {
                    request.getHeaders().set("Authorization", "Bearer " + apiKey);
                    request.getHeaders().set("Content-Type", "application/json");
                    return execution.execute(request, body);
                })
                .build();
    }

    @Bean
    public String openaiApiUrl() {
        return apiUrl;
    }

    @Bean
    public String openaiApiKey() {
        return apiKey;
    }
}
