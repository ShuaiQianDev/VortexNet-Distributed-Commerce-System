package com.example.demo.recommendation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Content-Based Filtering - Recommend similar products
 *
 * Core idea: Recommend products similar to what user has already purchased
 * Based on product features (category, price, rating, tags, etc.)
 */
@Component
@Slf4j
public class ContentBasedFiltering {

    @Autowired
    private SimilarityCalculator similarityCalculator;

    /**
     * Generate recommendations based on product similarity
     *
     * Algorithm:
     * 1. Get user's previously purchased products
     * 2. For each unvisited product, calculate similarity to purchased products
     * 3. Average similarity score becomes recommendation score
     * 4. Rank and return top K products
     */
    public List<Long> recommendProducts(
            UserProfile userProfile,
            List<ProductFeature> allProducts,
            int topK) {

        log.info("Generating content-based recommendations for user");

        // Step 1: Get user's purchased product features
        List<Long> purchasedIds = userProfile.getPurchasedProductIds();
        if (purchasedIds == null || purchasedIds.isEmpty()) {
            log.warn("User has no purchase history for content-based filtering");
            return Collections.emptyList();
        }

        Map<Long, ProductFeature> productMap = allProducts.stream()
                .collect(Collectors.toMap(ProductFeature::getProductId, p -> p));

        List<ProductFeature> purchasedProducts = purchasedIds.stream()
                .filter(productMap::containsKey)
                .map(productMap::get)
                .collect(Collectors.toList());

        // Step 2: Calculate similarity scores for all unvisited products
        Map<Long, Double> recommendationScores = new HashMap<>();
        Set<Long> visitedProducts = new HashSet<>(purchasedIds);
        if (userProfile.getViewedProductIds() != null) {
            visitedProducts.addAll(userProfile.getViewedProductIds());
        }

        for (ProductFeature candidateProduct : allProducts) {
            // Skip already visited products
            if (visitedProducts.contains(candidateProduct.getProductId())) {
                continue;
            }

            // Calculate average similarity to purchased products
            double totalSimilarity = 0.0;
            int count = 0;

            for (ProductFeature purchasedProduct : purchasedProducts) {
                double similarity = calculateProductSimilarity(purchasedProduct, candidateProduct);
                totalSimilarity += similarity;
                count++;
            }

            double averageSimilarity = count > 0 ? totalSimilarity / count : 0.0;

            // Boost score based on product quality and popularity
            double boostedScore = averageSimilarity * 0.7 +
                    candidateProduct.calculateQualityScore() * 0.2 +
                    candidateProduct.calculatePopularityScore() * 0.1;

            if (boostedScore > 0.1) {  // Only include products with meaningful similarity
                recommendationScores.put(candidateProduct.getProductId(), boostedScore);
            }
        }

        // Step 3: Return top K products by score
        return recommendationScores.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(topK)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Calculate comprehensive similarity between two products
     *
     * Combines:
     * - Feature vector similarity (price, rating, etc.)
     * - Category similarity
     * - Tag similarity
     */
    private double calculateProductSimilarity(ProductFeature product1, ProductFeature product2) {
        // Step 1: Calculate feature vector similarity using cosine similarity
        double[] vector1 = product1.getFeatureVector();
        double[] vector2 = product2.getFeatureVector();
        double vectorSimilarity = similarityCalculator.cosineSimilarity(vector1, vector2);

        // Step 2: Calculate category similarity
        double categorySimilarity = similarityCalculator.categorySimilarity(
                product1.getCategory(),
                product2.getCategory()
        );

        // Step 3: Calculate tag similarity
        Set<String> tags1 = new HashSet<>(product1.getTags() != null ? product1.getTags() : Collections.emptyList());
        Set<String> tags2 = new HashSet<>(product2.getTags() != null ? product2.getTags() : Collections.emptyList());
        double tagSimilarity = similarityCalculator.jaccardSimilarity(tags1, tags2);

        // Step 4: Combine similarities with weights
        return (vectorSimilarity * 0.5) +
                (categorySimilarity * 0.3) +
                (tagSimilarity * 0.2);
    }
}
