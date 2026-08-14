package com.example.demo.recommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * User Profile - Aggregated user features for recommendation
 *
 * Represents a user's historical behavior and preferences
 * Used by recommendation models to understand user interests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    private String userId;                          // User ID
    private List<Long> viewedProductIds;           // Products user viewed
    private List<Long> purchasedProductIds;        // Products user purchased
    private List<String> searchQueries;            // Search history
    private Map<Long, Double> productRatings;      // Product ratings (productId -> rating)
    private Map<String, Integer> categoryPreference; // Category preferences
    private double averageSpendingAmount;          // Average purchase amount
    private long lastActivityTime;                 // Last activity timestamp
    private int totalPurchases;                    // Total purchase count
    private double totalSpent;                     // Total money spent

    /**
     * Calculate user engagement score (0-1)
     * Based on activity frequency and purchase history
     */
    public double calculateEngagementScore() {
        double activityScore = Math.min(viewedProductIds.size() / 100.0, 1.0);
        double purchaseScore = Math.min(totalPurchases / 20.0, 1.0);
        return (activityScore * 0.4) + (purchaseScore * 0.6);
    }

    /**
     * Get user's primary interest category
     */
    public String getPrimaryCategory() {
        if (categoryPreference == null || categoryPreference.isEmpty()) {
            return "General";
        }
        return categoryPreference.entrySet().stream()
                .max((e1, e2) -> e1.getValue().compareTo(e2.getValue()))
                .map(Map.Entry::getKey)
                .orElse("General");
    }
}
