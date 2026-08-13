package com.example.demo.llm;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenLimiter {

    private final Map<String, Integer> userTokenUsage = new ConcurrentHashMap<>();

    private static final int FREE_USER_QUOTA = 10000;
    private static final int PAID_USER_QUOTA = 100000;

    public boolean allowRequest(String userId, int estimatedTokens) {
        int currentUsage = userTokenUsage.getOrDefault(userId, 0);
        int quota = PAID_USER_QUOTA;

        int remaining = quota - currentUsage;

        if (remaining < estimatedTokens) {
            log.warn("User {} exceeded quota. Used: {}, Quota: {}, Requested: {}",
                    userId, currentUsage, quota, estimatedTokens);
            return false;
        }

        log.debug("User {} quota check passed. Used: {}/{}",
                userId, currentUsage, quota);
        return true;
    }

    public void recordUsage(String userId, int tokensUsed) {
        userTokenUsage.put(userId,
                userTokenUsage.getOrDefault(userId, 0) + tokensUsed);

        int totalUsed = userTokenUsage.get(userId);
        log.info("User {} token usage recorded. Session: +{} tokens, Total: {} tokens",
                userId, tokensUsed, totalUsed);
    }

    public Map<String, Integer> getUserStats(String userId) {
        int used = userTokenUsage.getOrDefault(userId, 0);
        int quota = PAID_USER_QUOTA;

        return Map.of(
                "used_tokens", used,
                "quota", quota,
                "remaining_tokens", quota - used,
                "usage_percentage", (int)((double)used / quota * 100)
        );
    }

    public void resetMonthlyQuota(String userId) {
        userTokenUsage.put(userId, 0);
        log.info("Reset monthly quota for user {}", userId);
    }
}
