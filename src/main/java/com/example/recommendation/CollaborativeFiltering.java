package com.example.demo.recommendation;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Collaborative Filtering - Recommend based on similar users
 *
 * Core idea: If users A and B have similar purchase history,
 * recommend to A the products that B purchased but A hasn't
 */
@Component
@Slf4j
public class CollaborativeFiltering {

    /**
     * Find users similar to target user based on purchase history
     *
     * Similarity metric: Jaccard similarity on product IDs
     * Similar users = users who purchased similar products
     */
    public List<String> findSimilarUsers(
            String userId,
            UserProfile targetUser,
            Map<String, UserProfile> allUsers,
            int topK) {

        log.info("Finding {} similar users to {}", topK, userId);

        // Convert purchase history to set for similarity calculation
        Set<Long> targetPurchases = new HashSet<>(
                targetUser.getPurchasedProductIds() != null ?
                        targetUser.getPurchasedProductIds() :
                        Collections.emptyList()
        );

        if (targetPurchases.isEmpty()) {
            log.warn("User {} has no purchase history", userId);
            return Collections.emptyList();
        }

        // Calculate similarity with all other users
        Map<String, Double> userSimilarities = new HashMap<>();

        for (Map.Entry<String, UserProfile> entry : allUsers.entrySet()) {
            String otherUserId = entry.getKey();
            UserProfile otherUser = entry.getValue();

            // Skip self
            if (otherUserId.equals(userId)) {
                continue;
            }

            // Calculate Jaccard similarity
            Set<Long> otherPurchases = new HashSet<>(
                    otherUser.getPurchasedProductIds() != null ?
                            otherUser.getPurchasedProductIds() :
                            Collections.emptyList()
            );

            double similarity = jaccardSimilarity(targetPurchases, otherPurchases);
            if (similarity > 0) {
                userSimilarities.put(otherUserId, similarity);
            }
        }

        // Return top K similar users
        return userSimilarities.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Generate recommendations using collaborative filtering
     *
     * Algorithm:
     * 1. Find K similar users to target user
     * 2. Get products purchased by similar users but not by target user
     * 3. Rank by frequency (more users bought = higher rank)
     */
    public List<Long> recommendProducts(
            String userId,
            UserProfile targetUser,
            Map<String, UserProfile> allUsers,
            int topK,
            int similarUsersCount) {

        // Step 1: Find similar users
        List<String> similarUsers = findSimilarUsers(
                userId,
                targetUser,
                allUsers,
                similarUsersCount
        );

        if (similarUsers.isEmpty()) {
            log.info("No similar users found for {}", userId);
            return Collections.emptyList();
        }

        // Step 2: Collect products from similar users
        Set<Long> userPurchases = new HashSet<>(
                targetUser.getPurchasedProductIds() != null ?
                        targetUser.getPurchasedProductIds() :
                        Collections.emptyList()
        );

        Map<Long, Integer> productFrequency = new HashMap<>();

        for (String similarUserId : similarUsers) {
            UserProfile similarUser = allUsers.get(similarUserId);
            if (similarUser != null && similarUser.getPurchasedProductIds() != null) {
                for (Long productId : similarUser.getPurchasedProductIds()) {
                    // Only recommend products not already purchased
                    if (!userPurchases.contains(productId)) {
                        productFrequency.put(
                                productId,
                                productFrequency.getOrDefault(productId, 0) + 1
                        );
                    }
                }
            }
        }

        // Step 3: Rank by frequency and return top K
        return productFrequency.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Calculate Jaccard similarity between two product sets
     */
    private double jaccardSimilarity(Set<Long> set1, Set<Long> set2) {
        if (set1.isEmpty() || set2.isEmpty()) {
            return 0.0;
        }

        Set<Long> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<Long> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }
}
