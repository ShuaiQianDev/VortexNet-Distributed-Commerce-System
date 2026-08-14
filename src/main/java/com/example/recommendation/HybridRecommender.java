package com.example.demo.recommendation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Hybrid Recommender - Combines collaborative and content-based filtering
 *
 * Best of both worlds:
 * - Collaborative Filtering:
 *   Recommend products based on similar users
 *
 * - Content-Based Filtering:
 *   Recommend products similar to user's interests
 *
 * - Hybrid:
 *   Combine both recommendation strategies
 *
 * Final pipeline:
 *
 * User Profile
 *      |
 *      +----------------------+
 *      |                      |
 *      v                      v
 * Collaborative         Content-Based
 * Filtering             Filtering
 *      |                      |
 *      +----------+-----------+
 *                 |
 *                 v
 *          Weighted Ranking
 *                 |
 *                 v
 *       Remove Interacted Items
 *                 |
 *                 v
 *          Final Top-K
 */
@Component
@Slf4j
public class HybridRecommender {

    @Autowired
    private CollaborativeFiltering collaborativeFiltering;

    @Autowired
    private ContentBasedFiltering contentBasedFiltering;


    /**
     * Generate hybrid recommendations.
     *
     * Algorithm:
     *
     * 1. Get collaborative filtering recommendations
     * 2. Get content-based filtering recommendations
     * 3. Assign weighted scores
     * 4. Merge recommendations
     * 5. Remove products user already viewed/purchased
     * 6. Return top K
     */
    public List<Long> recommendProducts(
            String userId,
            UserProfile userProfile,
            Map<String, UserProfile> allUsers,
            List<ProductFeature> allProducts,
            int topK
    ) {

        log.info(
                "Generating hybrid recommendations for user: {}",
                userId
        );


        /*
         * ============================================================
         * Step 1: Collaborative Filtering
         * ============================================================
         */

        List<Long> collaborativeRecs =
                collaborativeFiltering.recommendProducts(
                        userId,
                        userProfile,
                        allUsers,
                        topK * 2,
                        5
                );

        log.info(
                "Collaborative filtering returned {} products",
                collaborativeRecs.size()
        );


        /*
         * ============================================================
         * Step 2: Content-Based Filtering
         * ============================================================
         */

        List<Long> contentBasedRecs =
                contentBasedFiltering.recommendProducts(
                        userProfile,
                        allProducts,
                        topK * 2
                );

        log.info(
                "Content-based filtering returned {} products",
                contentBasedRecs.size()
        );


        /*
         * ============================================================
         * Step 3: Merge Recommendations
         * ============================================================
         *
         * Collaborative Filtering weight = 40%
         * Content-Based Filtering weight = 60%
         *
         * Content-based gets higher weight because:
         *
         * - We may have very few users in this demo
         * - Collaborative filtering needs enough users
         * - User's own product history is more reliable
         */

        Map<Long, Double> mergedScores =
                new HashMap<>();


        /*
         * Add Collaborative Filtering scores.
         *
         * Higher-ranked products receive higher scores.
         */
        if (!collaborativeRecs.isEmpty()) {

            for (int i = 0;
                 i < collaborativeRecs.size();
                 i++) {

                Long productId =
                        collaborativeRecs.get(i);

                double rankScore =
                        (double)
                                (collaborativeRecs.size() - i)
                                / collaborativeRecs.size();

                double weightedScore =
                        rankScore * 0.4;

                mergedScores.put(
                        productId,
                        mergedScores.getOrDefault(
                                productId,
                                0.0
                        ) + weightedScore
                );
            }
        }


        /*
         * Add Content-Based Filtering scores.
         *
         * Content-based recommendations receive 60%
         * of the total hybrid score.
         */
        if (!contentBasedRecs.isEmpty()) {

            for (int i = 0;
                 i < contentBasedRecs.size();
                 i++) {

                Long productId =
                        contentBasedRecs.get(i);

                double rankScore =
                        (double)
                                (contentBasedRecs.size() - i)
                                / contentBasedRecs.size();

                double weightedScore =
                        rankScore * 0.6;

                mergedScores.put(
                        productId,
                        mergedScores.getOrDefault(
                                productId,
                                0.0
                        ) + weightedScore
                );
            }
        }


        /*
         * ============================================================
         * Step 4: Collect User's Interacted Products
         * ============================================================
         *
         * We do NOT want to recommend:
         *
         * - Products already purchased
         * - Products already viewed
         *
         * This filtering is intentionally done at the final
         * hybrid stage as an additional safety layer.
         */

        Set<Long> interactedProducts =
                new HashSet<>();


        // Add purchased products
        if (userProfile.getPurchasedProductIds() != null) {

            interactedProducts.addAll(
                    userProfile.getPurchasedProductIds()
            );
        }


        // Add viewed products
        if (userProfile.getViewedProductIds() != null) {

            interactedProducts.addAll(
                    userProfile.getViewedProductIds()
            );
        }


        log.info(
                "User {} has interacted with {} products",
                userId,
                interactedProducts.size()
        );


        /*
         * ============================================================
         * Step 5: Filter + Rank
         * ============================================================
         *
         * Remove already interacted products.
         *
         * Then:
         *
         * score DESC
         *
         * Finally:
         *
         * top K
         */

        List<Long> finalRecommendations =
                mergedScores.entrySet()
                        .stream()

                        // Remove viewed / purchased products
                        .filter(
                                entry ->
                                        !interactedProducts
                                                .contains(
                                                        entry.getKey()
                                                )
                        )

                        // Highest score first
                        .sorted(
                                (e1, e2) ->
                                        Double.compare(
                                                e2.getValue(),
                                                e1.getValue()
                                        )
                        )

                        // Return only top K
                        .limit(topK)

                        // Convert Map.Entry to product ID
                        .map(Map.Entry::getKey)

                        .collect(
                                Collectors.toList()
                        );


        /*
         * ============================================================
         * Step 6: Logging
         * ============================================================
         */

        log.info(
                "Generated {} final hybrid recommendations for user {}",
                finalRecommendations.size(),
                userId
        );


        log.info(
                "Final recommendation product IDs: {}",
                finalRecommendations
        );


        return finalRecommendations;
    }


    /**
     * Diversify recommendations to avoid repetition.
     *
     * Strategy:
     *
     * Maximum 2 products per category.
     *
     * Example:
     *
     * Electronics
     * Electronics
     * Electronics
     * Clothing
     *
     * becomes:
     *
     * Electronics
     * Electronics
     * Clothing
     */
    public List<ProductFeature> diversifyRecommendations(
            List<Long> productIds,
            Map<Long, ProductFeature> productFeatureMap
    ) {

        log.info(
                "Diversifying recommendations"
        );


        List<ProductFeature> recommendations =
                new ArrayList<>();


        Map<String, Integer> categoryCount =
                new HashMap<>();


        for (Long productId : productIds) {

            ProductFeature product =
                    productFeatureMap.get(productId);


            // Product does not exist
            if (product == null) {
                continue;
            }


            String category =
                    product.getCategory();


            int count =
                    categoryCount.getOrDefault(
                            category,
                            0
                    );


            /*
             * Limit products per category to 2.
             */
            if (count < 2) {

                recommendations.add(
                        product
                );

                categoryCount.put(
                        category,
                        count + 1
                );
            }
        }


        log.info(
                "Diversified recommendations from {} to {} products",
                productIds.size(),
                recommendations.size()
        );


        return recommendations;
    }
}
