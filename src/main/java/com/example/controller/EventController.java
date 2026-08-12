package com.example.demo.controller;

import com.example.demo.event.EventPublisher;
import com.example.demo.event.EventType;
import com.example.demo.feature.RealTimeFeatures;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@Slf4j
public class EventController {

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private RealTimeFeatures realTimeFeatures;

    @PostMapping("/track")
    public ResponseEntity<?> trackEvent(
            @RequestParam String userId,
            @RequestParam String eventType,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Double amount) {

        try {
            EventType type = EventType.valueOf(eventType);
            eventPublisher.publishUserEvent(userId, type, productId, amount);

            log.info("Event tracked: user={}, type={}", userId, eventType);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Event tracked successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Invalid event type"
            ));
        }
    }

    @PostMapping("/page-view")
    public ResponseEntity<?> trackPageView(
            @RequestParam String userId,
            @RequestParam Long productId) {

        eventPublisher.publishPageView(userId, productId);
        log.info("Page view tracked: user={}, product={}", userId, productId);

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PostMapping("/purchase")
    public ResponseEntity<?> trackPurchase(
            @RequestParam String userId,
            @RequestParam Long productId,
            @RequestParam Double amount) {

        eventPublisher.publishPurchase(userId, productId, amount);
        log.info("Purchase tracked: user={}, product={}, amount={}",
                userId, productId, amount);

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @PostMapping("/search")
    public ResponseEntity<?> trackSearch(
            @RequestParam String userId,
            @RequestParam String query) {

        eventPublisher.publishSearch(userId, query);
        log.info("Search tracked: user={}, query={}", userId, query);

        return ResponseEntity.ok(Map.of("status", "success"));
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> getMetrics() {
        Map<String, Object> metrics = realTimeFeatures.getMetricsSnapshot();
        return ResponseEntity.ok(metrics);
    }
}
