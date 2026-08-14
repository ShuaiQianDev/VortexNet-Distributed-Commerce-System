package com.example.demo.recommendation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.List;

/**
 * Product Feature - Features of a product for recommendation
 *
 * Encapsulates all product attributes needed for:
 * - Similarity calculation
 * - Content-based filtering
 * - Recommendation scoring
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFeature implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;                        // Product ID
    private String productName;                    // Product name
    private String category;                       // Product category (e.g., "Electronics", "Clothing")
    private double price;                          // Product price
    private double rating;                         // Average rating (0-5)
    private int reviewCount;                       // Number of reviews
    private int viewCount;                         // Total views
    private int purchaseCount;                     // Total purchases
    private List<String> tags;                     // Product tags (e.g., ["wireless", "durable"])
    private String brand;                          // Product brand
    private double popularity;                     // Popularity score (0-1)
    private double profitMargin;                   // Profit margin for ranking

    /**
     * Calculate product quality score (0-1)
     * Based on rating and review count
     */
    public double calculateQualityScore() {
        double ratingScore = rating / 5.0;  // Normalize rating to 0-1
        double reviewScore = Math.min(reviewCount / 100.0, 1.0);  // Normalize reviews
        return (ratingScore * 0.7) + (reviewScore * 0.3);
    }

    /**
     * Calculate product popularity score (0-1)
     * Based on view and purchase counts
     */
    public double calculatePopularityScore() {
        double viewScore = Math.min(viewCount / 1000.0, 1.0);
        double purchaseScore = Math.min(purchaseCount / 100.0, 1.0);
        return (viewScore * 0.4) + (purchaseScore * 0.6);
    }

    /**
     * Get product's feature vector for similarity calculation
     * Returns normalized features
     */
    public double[] getFeatureVector() {
        return new double[] {
                price / 10000.0,           // Normalize price to 0-1
                rating / 5.0,              // Normalize rating
                calculateQualityScore(),
                calculatePopularityScore(),
                popularity
        };
    }
}
