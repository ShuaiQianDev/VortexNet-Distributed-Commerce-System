package com.example.demo.feature;

import com.example.demo.kinesis.EventStreamService;
import com.example.demo.feature.FeatureExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Service
@Slf4j
public class RealTimeFeatures {

    @Autowired
    private EventStreamService eventStreamService;

    public Map<String, Object> getRealTimeMetrics() {
        return eventStreamService.getRealTimeStats();
    }

    public Map<String, Object> getMetricsSnapshot() {
        Map<String, Object> metrics = getRealTimeMetrics();
        log.info("Real-time metrics snapshot: {}", metrics);
        return metrics;
    }
}
