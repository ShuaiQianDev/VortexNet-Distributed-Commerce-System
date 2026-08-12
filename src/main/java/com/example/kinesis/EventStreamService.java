package com.example.demo.kinesis;

import com.example.demo.event.UserEvent;
import com.example.demo.feature.FeatureExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class EventStreamService {

    @Autowired
    private KinesisProducer kinesisProducer;

    @Autowired
    private FeatureExtractor featureExtractor;

    private static final AtomicLong processedEventsCount = new AtomicLong(0);

    @Scheduled(fixedRate = 1000)
    public void processEventStream() {
        List<UserEvent> batchEvents = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            UserEvent event = kinesisProducer.pollEvent();
            if (event == null) break;
            batchEvents.add(event);
        }

        if (!batchEvents.isEmpty()) {
            Map<String, Map<String, Object>> features =
                    featureExtractor.extractBatchFeatures(batchEvents);

            sendToRecommendationEngine(features);
            processedEventsCount.addAndGet(batchEvents.size());

            log.info("Processed {} events in batch, total processed: {}",
                    batchEvents.size(), processedEventsCount.get());
        }
    }

    private void sendToRecommendationEngine(Map<String, Map<String, Object>> features) {
        log.debug("Sending {} feature vectors to recommendation engine", features.size());
    }

    public Map<String, Object> getRealTimeStats() {
        return Map.of(
                "pending_events", kinesisProducer.getQueueSize(),
                "processed_events", processedEventsCount.get(),
                "timestamp", System.currentTimeMillis()
        );
    }
}
