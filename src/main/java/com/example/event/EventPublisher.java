package com.example.demo.event;

import com.example.demo.kinesis.KinesisProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EventPublisher {

    @Autowired
    private KinesisProducer kinesisProducer;

    public void publishUserEvent(String userId, EventType eventType,
                                 Long productId, Double amount) {
        UserEvent event = new UserEvent(
                "evt_" + System.nanoTime(),
                userId,
                eventType,
                productId,
                amount,
                System.currentTimeMillis(),
                "user_behavior"
        );

        kinesisProducer.putEvent(event);
        log.info("User event published: userId={}, eventType={}",
                userId, eventType);
    }

    public void publishPageView(String userId, Long productId) {
        publishUserEvent(userId, EventType.PAGE_VIEW, productId, null);
    }

    public void publishPurchase(String userId, Long productId, Double amount) {
        publishUserEvent(userId, EventType.PURCHASE, productId, amount);
    }

    public void publishSearch(String userId, String searchQuery) {
        UserEvent event = new UserEvent(
                "evt_" + System.nanoTime(),
                userId,
                EventType.SEARCH,
                null,
                null,
                System.currentTimeMillis(),
                "query:" + searchQuery
        );
        kinesisProducer.putEvent(event);
    }
}
