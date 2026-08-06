package com.example.demo.service;

import com.example.demo.entity.IdempotencyKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class IdempotencyService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final long EXPIRY_SECONDS = 86400; // 24 hours

    /**
     * 检查是否已经处理过这个请求
     */
    public Optional<String> getExistingResponse(String clientRequestId) {
        String cacheKey = "idempotency:" + clientRequestId;
        String cachedResponse = redisTemplate.opsForValue().get(cacheKey);

        if (cachedResponse != null) {
            log.info("Cache hit in Redis for idempotency key: {}", clientRequestId);
            return Optional.of(cachedResponse);
        }

        log.info("Cache miss in Redis for idempotency key: {}", clientRequestId);
        return Optional.empty();
    }

    /**
     * 记录这是一个新请求
     */
    public void recordRequest(String clientRequestId) {
        log.info("Recording new request for key: {}", clientRequestId);
        // 只存 Redis，不存数据库
    }

    /**
     * 保存处理结果
     */
    public void recordResponse(String clientRequestId, String response) {
        String cacheKey = "idempotency:" + clientRequestId;
        redisTemplate.opsForValue().set(cacheKey, response, EXPIRY_SECONDS, TimeUnit.SECONDS);
        log.info("Response cached for idempotency key: {} (expires in {} seconds)", clientRequestId, EXPIRY_SECONDS);
    }
}
