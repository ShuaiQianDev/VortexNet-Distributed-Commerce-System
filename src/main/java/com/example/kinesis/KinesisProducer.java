package com.example.demo.kinesis;

import com.example.demo.event.UserEvent;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;

@Service
@Slf4j
public class KinesisProducer {

    // 模拟 Kinesis Stream
    private static final LinkedBlockingQueue<UserEvent> eventStream =
            new LinkedBlockingQueue<>(10000);

    public void putEvent(UserEvent event) {
        try {
            eventStream.put(event);

            log.info("Event published to Kinesis stream: eventType={}, userId={}, timestamp={}ms",
                    event.getEventType(), event.getUserId(),
                    System.currentTimeMillis() - event.getTimestamp());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Failed to publish event to Kinesis", e);
            throw new RuntimeException("Event publish failed", e);
        }
    }

    public UserEvent pollEvent() {
        return eventStream.poll();
    }

    public int getQueueSize() {
        return eventStream.size();
    }

    public void publishBatch(List<UserEvent> events) {
        events.forEach(this::putEvent);
        log.info("Batch of {} events published to Kinesis", events.size());
    }
}
