package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.service.CheckoutAssistantService;
import com.example.demo.service.GPTSearchService;
import com.example.demo.llm.TokenLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gpt")
@Slf4j
public class GPTController {

    @Autowired
    private GPTSearchService gptSearchService;

    @Autowired
    private CheckoutAssistantService checkoutAssistantService;

    @Autowired
    private TokenLimiter tokenLimiter;

    @PostMapping("/search")
    public ResponseEntity<?> intelligentSearch(
            @RequestParam String userId,
            @RequestParam String query) {

        long startTime = System.currentTimeMillis();

        try {
            log.info("Intelligent search request from user: {}, query: {}", userId, query);

            List<Product> results = gptSearchService.intelligentSearch(userId, query);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Search completed in {}ms, found {} products", duration, results.size());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "query", query,
                    "results", results,
                    "duration_ms", duration
            ));

        } catch (Exception e) {
            log.error("Search failed", e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/checkout-suggestions")
    public ResponseEntity<?> checkoutSuggestions(
            @RequestParam String userId,
            @RequestBody List<Product> cartItems) {

        try {
            log.info("Checkout suggestions for user: {}, items: {}", userId, cartItems.size());

            Map<String, Object> suggestions =
                    checkoutAssistantService.getDetailedSuggestions(userId, cartItems);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "suggestions", suggestions
            ));

        } catch (Exception e) {
            log.error("Checkout suggestions failed", e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/usage")
    public ResponseEntity<?> getUsage(
            @RequestParam String userId) {

        try {
            Map<String, Integer> stats = tokenLimiter.getUserStats(userId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "userId", userId,
                    "stats", stats
            ));

        } catch (Exception e) {
            log.error("Get usage failed", e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}
