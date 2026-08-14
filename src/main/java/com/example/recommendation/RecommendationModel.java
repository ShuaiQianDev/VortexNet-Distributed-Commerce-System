package com.example.demo.recommendation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Recommendation Model - Main orchestrator for all recommendation logic
 *
 * Coordinates:
 * - User profile management
 * - Product feature management
 * - Hybrid recommendation generation
 * - Real-time recommendations
 */
@Component
@Slf4j
public class RecommendationModel {

    @Autowired
    private HybridRecommender hybridRecommender;

    // In-memory storage
    // In production, this would be replaced by a database or cache.
    private final Map<String, UserProfile> userProfiles =
            new HashMap<>();

    private final Map<Long, ProductFeature> productFeatures =
            new HashMap<>();


    /**
     * Update or create user profile.
     */
    public void updateUserProfile(
            String userId,
            UserProfile profile
    ) {

        userProfiles.put(
                userId,
                profile
        );

        log.info(
                "Updated profile for user: {}",
                userId
        );
    }


    /**
     * Get existing user profile.
     *
     * Returns null if the user does not exist.
     */
    public UserProfile getUserProfile(
            String userId
    ) {

        return userProfiles.get(userId);
    }


    /**
     * Check whether a user profile exists.
     */
    public boolean hasUserProfile(
            String userId
    ) {

        return userProfiles.containsKey(userId);
    }


    /**
     * Update or create product feature.
     */
    public void updateProductFeature(
            Long productId,
            ProductFeature feature
    ) {

        productFeatures.put(
                productId,
                feature
        );

        log.info(
                "Updated features for product: {}",
                productId
        );
    }


    /**
     * Generate real-time recommendations.
     */
    public List<Long> generateRecommendations(
            String userId,
            int topK
    ) {

        long startTime =
                System.currentTimeMillis();

        log.info(
                "Generating recommendations for user: {}, topK: {}",
                userId,
                topK
        );


        // Get user profile
        UserProfile userProfile =
                userProfiles.get(userId);


        // Cold-start fallback
        if (userProfile == null) {

            log.warn(
                    "User profile not found: {}",
                    userId
            );

            return getPopularProducts(topK);
        }


        // Generate hybrid recommendations
        List<Long> recommendations =
                hybridRecommender.recommendProducts(
                        userId,
                        userProfile,
                        userProfiles,
                        new ArrayList<>(
                                productFeatures.values()
                        ),
                        topK
                );


        long duration =
                System.currentTimeMillis()
                        - startTime;


        log.info(
                "Generated recommendations in {}ms",
                duration
        );


        return recommendations;
    }


    /**
     * Get recommendations with product features.
     */
    public List<ProductFeature> getRecommendationDetails(
            String userId,
            int topK
    ) {

        List<Long> recommendedProductIds =
                generateRecommendations(
                        userId,
                        topK
                );


        return recommendedProductIds
                .stream()
                .map(productFeatures::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    /**
     * Fallback:
     * Get popular products.
     */
    private List<Long> getPopularProducts(
            int topK
    ) {

        log.info(
                "Returning popular products as fallback"
        );


        return productFeatures
                .values()
                .stream()
                .sorted(
                        (p1, p2) ->
                                Double.compare(
                                        p2.getPopularity(),
                                        p1.getPopularity()
                                )
                )
                .limit(topK)
                .map(ProductFeature::getProductId)
                .collect(Collectors.toList());
    }


    /**
     * Get trending products.
     */
    public List<Long> getTrendingProducts(
            int topK
    ) {

        return productFeatures
                .values()
                .stream()
                .sorted(
                        (p1, p2) ->
                                Integer.compare(
                                        p2.getViewCount(),
                                        p1.getViewCount()
                                )
                )
                .limit(topK)
                .map(ProductFeature::getProductId)
                .collect(Collectors.toList());
    }


    /**
     * Get model statistics.
     */
    public Map<String, Object> getModelStats() {

        return Map.of(
                "total_users",
                userProfiles.size(),

                "total_products",
                productFeatures.size(),

                "average_user_engagement",
                calculateAverageEngagement()
        );
    }


    /**
     * Calculate average user engagement.
     */
    private double calculateAverageEngagement() {

        if (userProfiles.isEmpty()) {
            return 0.0;
        }


        return userProfiles
                .values()
                .stream()
                .mapToDouble(
                        UserProfile::calculateEngagementScore
                )
                .average()
                .orElse(0.0);
    }
}
