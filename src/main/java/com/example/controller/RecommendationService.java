package com.example.demo.service;

import com.example.demo.entity.Product;
import com.example.demo.recommendation.ProductFeature;
import com.example.demo.recommendation.RecommendationModel;
import com.example.demo.recommendation.UserProfile;

import jakarta.annotation.PostConstruct;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RecommendationService {

    @Autowired
    private RecommendationModel recommendationModel;

    @Autowired
    private ProductService productService;


    // ============================================================
    // Initialization
    // ============================================================

    /**
     * Initialize recommendation system after Spring starts.
     *
     * Converts Product objects from ProductService
     * into ProductFeature objects used by the recommendation model.
     */
    @PostConstruct
    public void initializeRecommendationSystem() {

        log.info(
                "Initializing recommendation system..."
        );

        initializeProductFeatures();

        log.info(
                "Recommendation system initialized successfully"
        );
    }


    /**
     * Initialize ProductFeature objects.
     *
     * In production, these features would normally come from:
     *
     * - Database
     * - Feature Store
     * - Kafka/Kinesis
     * - ML feature pipeline
     *
     * For this project, we generate them from Product objects.
     */
    public void initializeProductFeatures() {

        log.info(
                "Initializing product features..."
        );

        List<Product> products =
                productService.getAllProducts();


        for (Product product : products) {

            ProductFeature feature =
                    new ProductFeature();


            // ----------------------------------------------------
            // Basic product information
            // ----------------------------------------------------

            feature.setProductId(
                    product.getId()
            );

            feature.setProductName(
                    product.getName()
            );


            // ----------------------------------------------------
            // Price
            // ----------------------------------------------------

            double price =
                    product.getPrice() != null
                            ? product.getPrice()
                            : 0.0;

            feature.setPrice(
                    price
            );


            // ----------------------------------------------------
            // Recommendation features
            //
            // These are demo values because the current Product
            // entity does not contain rating/review/view/purchase
            // fields.
            // ----------------------------------------------------

            feature.setRating(
                    4.0
            );

            feature.setReviewCount(
                    50
            );


            /*
             * Use stock as a temporary proxy for view count.
             *
             * In a production system this would come from
             * actual user interaction data.
             */
            int stock =
                    product.getStock() != null
                            ? product.getStock()
                            : 0;

            feature.setViewCount(
                    stock
            );


            /*
             * Demo purchase count.
             *
             * Later this should come from the database
             * or event stream.
             */
            feature.setPurchaseCount(
                    10
            );


            // ----------------------------------------------------
            // Tags
            // ----------------------------------------------------

            List<String> tags =
                    new ArrayList<>();

            String productName =
                    product.getName() != null
                            ? product.getName().toLowerCase()
                            : "";

            /*
             * Simple demo tag generation.
             *
             * Laptop:
             * [electronics, computer]
             *
             * Mouse:
             * [electronics, accessory]
             *
             * Keyboard:
             * [electronics, accessory]
             *
             * Monitor:
             * [electronics, display]
             *
             * Headset:
             * [electronics, audio]
             */

            if (productName.contains("laptop")) {

                tags.add("electronics");
                tags.add("computer");

            } else if (productName.contains("mouse")) {

                tags.add("electronics");
                tags.add("accessory");

            } else if (productName.contains("keyboard")) {

                tags.add("electronics");
                tags.add("accessory");

            } else if (productName.contains("monitor")) {

                tags.add("electronics");
                tags.add("display");

            } else if (productName.contains("headset")) {

                tags.add("electronics");
                tags.add("audio");

            } else {

                tags.add("electronics");
            }


            feature.setTags(
                    tags
            );


            // ----------------------------------------------------
            // Brand
            // ----------------------------------------------------

            feature.setBrand(
                    "VortexNet"
            );


            // ----------------------------------------------------
            // Popularity
            // ----------------------------------------------------

            double popularity =
                    calculatePopularity(
                            stock
                    );

            feature.setPopularity(
                    popularity
            );


            // ----------------------------------------------------
            // Profit margin
            // ----------------------------------------------------

            feature.setProfitMargin(
                    0.20
            );


            // ----------------------------------------------------
            // Register ProductFeature
            // ----------------------------------------------------

            recommendationModel.updateProductFeature(
                    product.getId(),
                    feature
            );


            log.info(
                    "Registered product feature: id={}, name={}, price={}, popularity={}",
                    product.getId(),
                    product.getName(),
                    feature.getPrice(),
                    feature.getPopularity()
            );
        }


        log.info(
                "Initialized {} product features",
                products.size()
        );
    }


    /**
     * Calculate a simple popularity score.
     *
     * This is only a demo implementation.
     */
    private double calculatePopularity(
            int stock
    ) {

        return Math.min(
                stock / 200.0,
                1.0
        );
    }


    // ============================================================
    // Recommendation
    // ============================================================

    /**
     * Get personalized recommendations for a user.
     */
    public List<Product> getRecommendationsForUser(
            String userId,
            int topK
    ) {

        log.info(
                "Getting recommendations for user: {}, topK: {}",
                userId,
                topK
        );


        // --------------------------------------------------------
        // Step 1
        // Generate recommended product IDs
        // --------------------------------------------------------

        List<Long> recommendedProductIds =
                recommendationModel.generateRecommendations(
                        userId,
                        topK
                );


        // --------------------------------------------------------
        // Step 2
        // Fallback
        // --------------------------------------------------------

        if (recommendedProductIds.isEmpty()) {

            log.info(
                    "No personalized recommendations for user {}, " +
                            "returning popular products",
                    userId
            );

            return getPopularProducts(
                    topK
            );
        }


        // --------------------------------------------------------
        // Step 3
        // Convert IDs to Product objects
        // --------------------------------------------------------

        List<Product> recommendations =
                recommendedProductIds
                        .stream()
                        .map(productService::getProductById)
                        .filter(product -> product != null)
                        .collect(Collectors.toList());


        log.info(
                "Returning {} recommendations to user: {}",
                recommendations.size(),
                userId
        );


        return recommendations;
    }


    // ============================================================
    // Popular Products
    // ============================================================

    /**
     * Get popular products.
     *
     * Current Product entity does not contain a real popularity
     * field, so we temporarily sort by stock.
     */
    public List<Product> getPopularProducts(
            int topK
    ) {

        log.info(
                "Getting popular products"
        );


        return productService
                .getAllProducts()
                .stream()
                .sorted(
                        (p1, p2) ->
                                Integer.compare(
                                        p2.getStock() != null
                                                ? p2.getStock()
                                                : 0,

                                        p1.getStock() != null
                                                ? p1.getStock()
                                                : 0
                                )
                )
                .limit(topK)
                .collect(Collectors.toList());
    }


    // ============================================================
    // Trending Products
    // ============================================================

    /**
     * Get trending products.
     *
     * Since the current Product entity does not contain
     * real-time view statistics, this demo implementation
     * uses stock as a temporary ranking signal.
     */
    public List<Product> getTrendingProducts(
            int topK
    ) {

        log.info(
                "Getting trending products"
        );


        return productService
                .getAllProducts()
                .stream()
                .sorted(
                        (p1, p2) ->
                                Integer.compare(
                                        p2.getStock() != null
                                                ? p2.getStock()
                                                : 0,

                                        p1.getStock() != null
                                                ? p1.getStock()
                                                : 0
                                )
                )
                .limit(topK)
                .collect(Collectors.toList());
    }


    // ============================================================
    // User Interaction
    // ============================================================

    /**
     * Record user interaction.
     *
     * Supported interaction types:
     *
     * VIEW
     * PURCHASE
     */
    public void recordUserInteraction(
            String userId,
            Long productId,
            String interactionType
    ) {

        log.info(
                "Recording user interaction: " +
                        "userId={}, productId={}, type={}",
                userId,
                productId,
                interactionType
        );


        // --------------------------------------------------------
        // Get existing profile
        // --------------------------------------------------------

        UserProfile profile =
                recommendationModel.getUserProfile(
                        userId
                );


        // --------------------------------------------------------
        // Create profile if user does not exist
        // --------------------------------------------------------

        if (profile == null) {

            profile =
                    createUserProfile(
                            userId
                    );

            log.info(
                    "Created new user profile for user: {}",
                    userId
            );
        }


        // --------------------------------------------------------
        // Process interaction
        // --------------------------------------------------------

        switch (
                interactionType.toUpperCase()
        ) {


            // ====================================================
            // VIEW
            // ====================================================

            case "VIEW":

                if (
                        profile.getViewedProductIds()
                                == null
                ) {

                    profile.setViewedProductIds(
                            new ArrayList<>()
                    );
                }


                /*
                 * Avoid duplicate view history.
                 */
                if (
                        !profile
                                .getViewedProductIds()
                                .contains(productId)
                ) {

                    profile
                            .getViewedProductIds()
                            .add(productId);
                }

                break;


            // ====================================================
            // PURCHASE
            // ====================================================

            case "PURCHASE":

                if (
                        profile.getPurchasedProductIds()
                                == null
                ) {

                    profile.setPurchasedProductIds(
                            new ArrayList<>()
                    );
                }


                /*
                 * Avoid duplicate purchase history.
                 */
                if (
                        !profile
                                .getPurchasedProductIds()
                                .contains(productId)
                ) {

                    profile
                            .getPurchasedProductIds()
                            .add(productId);


                    profile.setTotalPurchases(
                            profile.getTotalPurchases()
                                    + 1
                    );
                }

                break;


            // ====================================================
            // UNKNOWN
            // ====================================================

            default:

                log.warn(
                        "Unknown interaction type: {}",
                        interactionType
                );

                return;
        }


        // --------------------------------------------------------
        // Update last activity time
        // --------------------------------------------------------

        profile.setLastActivityTime(
                System.currentTimeMillis()
        );


        // --------------------------------------------------------
        // Save profile
        // --------------------------------------------------------

        recommendationModel.updateUserProfile(
                userId,
                profile
        );


        // --------------------------------------------------------
        // Debug information
        // --------------------------------------------------------

        log.info(
                "User {} profile updated. " +
                        "Viewed products: {}, " +
                        "Purchased products: {}, " +
                        "Total purchases: {}",
                userId,
                profile.getViewedProductIds(),
                profile.getPurchasedProductIds(),
                profile.getTotalPurchases()
        );
    }


    // ============================================================
    // Create User Profile
    // ============================================================

    /**
     * Create an empty user profile.
     */
    private UserProfile createUserProfile(
            String userId
    ) {

        UserProfile profile =
                new UserProfile();


        profile.setUserId(
                userId
        );


        profile.setViewedProductIds(
                new ArrayList<>()
        );


        profile.setPurchasedProductIds(
                new ArrayList<>()
        );


        profile.setSearchQueries(
                new ArrayList<>()
        );


        profile.setTotalPurchases(
                0
        );


        profile.setTotalSpent(
                0.0
        );


        profile.setAverageSpendingAmount(
                0.0
        );


        profile.setLastActivityTime(
                System.currentTimeMillis()
        );


        return profile;
    }


    // ============================================================
    // Statistics
    // ============================================================

    /**
     * Get recommendation system statistics.
     */
    public Map<String, Object> getRecommendationStats() {

        log.info(
                "Getting recommendation system statistics"
        );


        return recommendationModel
                .getModelStats();
    }
}
