package com.example.demo.feature;

import com.example.demo.event.UserEvent;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Service
@Slf4j
public class FeatureExtractor {

    public Map<String, Object> extractFeatures(UserEvent event) {
        Map<String, Object> features = new HashMap<>();

        features.put("user_id", event.getUserId());
        features.put("product_id", event.getProductId());
        features.put("event_type", event.getEventType().toString());
        features.put("timestamp", event.getTimestamp());

        long eventTime = event.getTimestamp();
        features.put("hour_of_day", (eventTime / 3600000) % 24);
        features.put("day_of_week", getDayOfWeek(eventTime));

        switch(event.getEventType()) {
            case PURCHASE:
                features.put("purchase_amount", event.getAmount());
                features.put("is_purchase", 1);
                break;
            case PRODUCT_CLICK:
                features.put("engagement_signal", 1);
                break;
            case PAGE_VIEW:
                features.put("view_duration", 0);
                break;
            default:
                break;
        }

        log.debug("Features extracted for event: {}", features);
        return features;
    }

    public Map<String, Map<String, Object>> extractBatchFeatures(
            List<UserEvent> events) {
        Map<String, Map<String, Object>> batchFeatures = new HashMap<>();

        for (UserEvent event : events) {
            batchFeatures.put(event.getEventId(), extractFeatures(event));
        }

        log.info("Extracted features for {} events", events.size());
        return batchFeatures;
    }

    private int getDayOfWeek(long timestamp) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return cal.get(java.util.Calendar.DAY_OF_WEEK);
    }
}
