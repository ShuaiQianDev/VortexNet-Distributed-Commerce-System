package com.example.demo.service;

import com.example.demo.llm.OpenAIClient;
import com.example.demo.llm.TokenLimiter;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * LLM Rate Limiter Service
 *
 * Provides protected OpenAI API calls with:
 * - Rate limiting (prevent API overload)
 * - Circuit breaker (automatic fallback on failure)
 * - Token limiting (control costs)
 * - Graceful degradation (99.9% availability)
 */
@Service
@Slf4j
public class LLMRateLimiter {

    @Autowired
    private OpenAIClient openaiClient;

    @Autowired
    private TokenLimiter tokenLimiter;

    /**
     * Protected GPT call with circuit breaker and rate limiting
     *
     * Flow:
     * 1. Check token quota
     * 2. Call OpenAI API (protected by @CircuitBreaker and @RateLimiter)
     * 3. Record actual token usage
     * 4. Return response (or fallback if circuit opens)
     *
     * @param userId User identifier
     * @param prompt User's question/request
     * @return GPT's response or fallback message
     */
    @CircuitBreaker(
            name = "gptService",
            fallbackMethod = "askGPTFallback"
    )
    @RateLimiter(
            name = "gptApi",
            fallbackMethod = "askGPTFallback"
    )
    public String askGPT(String userId, String prompt) {
        // Step 1: Estimate tokens needed for this request
        int estimatedTokens = estimateTokens(prompt);

        // Step 2: Check if user has sufficient token quota
        if (!tokenLimiter.allowRequest(userId, estimatedTokens)) {
            throw new RuntimeException("User token quota exceeded");
        }

        log.info("Calling OpenAI API for user: {}", userId);

        // Step 3: Call OpenAI (protected by @CircuitBreaker)
        String response = openaiClient.askGPT(prompt, 300);

        // Step 4: Estimate actual tokens used
        int actualTokens = estimateTokens(response);

        // Step 5: Record usage for cost tracking
        tokenLimiter.recordUsage(userId, estimatedTokens + actualTokens);

        return response;
    }

    /**
     * Fallback method called when circuit breaker opens or rate limiter is exceeded
     * Provides graceful degradation to ensure 99.9% availability
     *
     * @param userId User identifier
     * @param prompt Original user request
     * @param ex Exception that triggered fallback
     * @return Pre-defined helpful message
     */
    public String askGPTFallback(String userId, String prompt, Exception ex) {
        log.warn("Circuit breaker activated for user: {}. Reason: {}",
                userId, ex.getMessage());

        // Return appropriate fallback message based on request type
        if (prompt.contains("search")) {
            return "Sorry, search service is temporarily unavailable. Please try again later. " +
                    "Recommended products: Laptop, Phone, Headset";
        } else if (prompt.contains("recommend")) {
            return "Based on our recommendation algorithm, this product is very popular.";
        } else {
            return "Service is under maintenance. Please try again later.";
        }
    }

    /**
     * Estimate number of tokens in the given text
     * Simple estimation: 1 English token ≈ 4 characters, 1 Chinese token ≈ 1 character
     *
     * Note: This is a rough estimation. Actual token count may vary.
     * OpenAI provides precise token counting via their tokenizer.
     *
     * @param text Input text to estimate tokens for
     * @return Estimated token count
     */
    private int estimateTokens(String text) {
        // Return 0 for null input
        if (text == null) return 0;

        // Count English characters (ASCII < 128)
        int englishChars = (int) text.chars()
                .filter(c -> c < 128)
                .count();

        // Count Chinese/non-ASCII characters
        int chineseChars = text.length() - englishChars;

        // Simple estimation: English 4 chars per token, Chinese 1 char per token
        return (englishChars / 4) + chineseChars;
    }
}
