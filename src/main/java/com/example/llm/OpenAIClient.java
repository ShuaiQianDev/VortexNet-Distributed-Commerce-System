package com.example.demo.llm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OpenAIClient {

    @Autowired
    private RestTemplate openaiRestTemplate;

    @Autowired
    private String openaiApiUrl;

    public String askGPT(String prompt, Integer maxTokens) {
        try {
            OpenAIRequest request = buildRequest(prompt, maxTokens);

            log.info("Sending request to OpenAI API: model={}, tokens={}",
                    request.getModel(), request.getMax_tokens());

            OpenAIResponse response = openaiRestTemplate.postForObject(
                    openaiApiUrl,
                    request,
                    OpenAIResponse.class
            );

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                log.error("Empty response from OpenAI API");
                throw new RuntimeException("Empty response from OpenAI");
            }

            String answer = response.getChoices().get(0).getMessage().getContent();
            int tokensUsed = response.getUsage().getTotal_tokens();

            log.info("OpenAI response received: tokens_used={}, answer_length={}",
                    tokensUsed, answer.length());

            return answer;

        } catch (RestClientException e) {
            log.error("Failed to call OpenAI API", e);
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage());
        }
    }

    private OpenAIRequest buildRequest(String prompt, Integer maxTokens) {
        List<OpenAIRequest.Message> messages = new ArrayList<>();
        messages.add(new OpenAIRequest.Message("user", prompt));

        OpenAIRequest request = new OpenAIRequest();
        request.setModel("gpt-3.5-turbo");
        request.setMessages(messages);
        request.setMax_tokens(maxTokens != null ? maxTokens : 500);
        request.setTemperature(0.3);
        request.setTop_p(0.9);

        return request;
    }
}
