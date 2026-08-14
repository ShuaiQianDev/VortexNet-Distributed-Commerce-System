package com.example.demo.recommendation;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

/**
 * Similarity Calculator
 *
 * Calculates similarity between products using:
 * - Cosine similarity
 * - Euclidean similarity
 * - Jaccard similarity
 * - Category similarity
 */
@Component
@Slf4j
public class SimilarityCalculator {

    /**
     * Calculate cosine similarity between two feature vectors.
     *
     * Formula:
     *
     * cos(A, B) = (A · B) / (||A|| * ||B||)
     *
     * Range:
     * 0 = completely different direction
     * 1 = identical direction
     */
    public double cosineSimilarity(
            double[] vector1,
            double[] vector2
    ) {

        // Null check
        if (vector1 == null || vector2 == null) {
            return 0.0;
        }

        // Dimension check
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException(
                    "Vectors must have same dimension"
            );
        }

        // Empty vector
        if (vector1.length == 0) {
            return 0.0;
        }

        // Calculate dot product
        double dotProduct = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
        }

        // Calculate magnitudes
        double magnitude1 =
                Math.sqrt(dotProduct(vector1, vector1));

        double magnitude2 =
                Math.sqrt(dotProduct(vector2, vector2));

        // Avoid division by zero
        if (magnitude1 == 0.0 || magnitude2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }


    /**
     * Calculate Euclidean similarity.
     *
     * First calculate Euclidean distance:
     *
     * distance = sqrt(sum((A_i - B_i)^2))
     *
     * Then convert distance to similarity:
     *
     * similarity = 1 / (1 + distance)
     *
     * Range:
     * 0 < similarity <= 1
     */
    public double euclideanSimilarity(
            double[] vector1,
            double[] vector2
    ) {

        // Null check
        if (vector1 == null || vector2 == null) {
            return 0.0;
        }

        // Dimension check
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException(
                    "Vectors must have same dimension"
            );
        }

        if (vector1.length == 0) {
            return 0.0;
        }

        // Calculate squared distance
        double sumSquaredDifference = 0.0;

        for (int i = 0; i < vector1.length; i++) {

            double difference =
                    vector1[i] - vector2[i];

            sumSquaredDifference +=
                    difference * difference;
        }

        // Euclidean distance
        double distance =
                Math.sqrt(sumSquaredDifference);

        // Convert distance to similarity
        return 1.0 / (1.0 + distance);
    }


    /**
     * Calculate Jaccard similarity between two sets.
     *
     * Formula:
     *
     * Jaccard =
     * |intersection| / |union|
     *
     * Example:
     *
     * Set A = {wireless, computer, usb}
     * Set B = {wireless, computer}
     *
     * intersection = 2
     * union = 3
     *
     * similarity = 2 / 3
     */
    public double jaccardSimilarity(
            Set<String> set1,
            Set<String> set2
    ) {

        // Null check
        if (set1 == null || set2 == null) {
            return 0.0;
        }

        // Both empty
        if (set1.isEmpty() && set2.isEmpty()) {
            return 0.0;
        }

        // Create copies so original sets are not modified
        Set<String> intersection =
                new HashSet<>(set1);

        intersection.retainAll(set2);

        Set<String> union =
                new HashSet<>(set1);

        union.addAll(set2);

        // Avoid division by zero
        if (union.isEmpty()) {
            return 0.0;
        }

        return (double) intersection.size()
                / union.size();
    }


    /**
     * Calculate category similarity.
     *
     * Same category:
     * 1.0
     *
     * Different category:
     * 0.0
     *
     * Missing category:
     * 0.0
     *
     * Null-safe implementation.
     */
    public double categorySimilarity(
            String category1,
            String category2
    ) {

        // IMPORTANT:
        // Prevent NullPointerException when category is null.
        if (category1 == null || category2 == null) {
            return 0.0;
        }

        // Remove accidental whitespace
        String normalizedCategory1 =
                category1.trim();

        String normalizedCategory2 =
                category2.trim();

        // Empty category
        if (normalizedCategory1.isEmpty()
                || normalizedCategory2.isEmpty()) {

            return 0.0;
        }

        // Case-insensitive comparison
        if (normalizedCategory1.equalsIgnoreCase(
                normalizedCategory2
        )) {

            return 1.0;
        }

        return 0.0;
    }


    /**
     * Calculate dot product.
     *
     * A · B =
     * A1*B1 + A2*B2 + ... + An*Bn
     */
    private double dotProduct(
            double[] vector1,
            double[] vector2
    ) {

        if (vector1 == null || vector2 == null) {
            return 0.0;
        }

        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException(
                    "Vectors must have same dimension"
            );
        }

        double result = 0.0;

        for (int i = 0; i < vector1.length; i++) {
            result += vector1[i] * vector2[i];
        }

        return result;
    }
}
