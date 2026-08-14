package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;


    /**
     * Get personalized recommendations for a user.
     *
     * Example:
     * GET /api/recommendations/user/1?topK=5
     */
    @GetMapping("/user/{userId}")
    public List<Product> getRecommendations(
            @PathVariable String userId,
            @RequestParam(defaultValue = "5") int topK
    ) {

        return recommendationService.getRecommendationsForUser(
                userId,
                topK
        );
    }


    /**
     * Get popular products.
     *
     * Example:
     * GET /api/recommendations/popular?topK=5
     */
    @GetMapping("/popular")
    public List<Product> getPopularProducts(
            @RequestParam(defaultValue = "5") int topK
    ) {

        return recommendationService.getPopularProducts(topK);
    }


    /**
     * Get trending products.
     *
     * Example:
     * GET /api/recommendations/trending?topK=5
     */
    @GetMapping("/trending")
    public List<Product> getTrendingProducts(
            @RequestParam(defaultValue = "5") int topK
    ) {

        return recommendationService.getTrendingProducts(topK);
    }


    /**
     * Record user interaction.
     *
     * Example:
     * POST /api/recommendations/interaction
     *
     * Request body:
     * {
     *     "userId": "1",
     *     "productId": 2,
     *     "interactionType": "VIEW"
     * }
     */
    @PostMapping("/interaction")
    public String recordInteraction(
            @RequestBody InteractionRequest request
    ) {

        recommendationService.recordUserInteraction(
                request.getUserId(),
                request.getProductId(),
                request.getInteractionType()
        );

        return "Interaction recorded successfully";
    }


    /**
     * Get recommendation model statistics.
     *
     * Example:
     * GET /api/recommendations/stats
     */
    @GetMapping("/stats")
    public Map<String, Object> getRecommendationStats() {

        return recommendationService.getRecommendationStats();
    }


    /**
     * Request body for user interaction.
     */
    public static class InteractionRequest {

        private String userId;

        private Long productId;

        private String interactionType;


        public InteractionRequest() {
        }


        public String getUserId() {
            return userId;
        }


        public void setUserId(String userId) {
            this.userId = userId;
        }


        public Long getProductId() {
            return productId;
        }


        public void setProductId(Long productId) {
            this.productId = productId;
        }


        public String getInteractionType() {
            return interactionType;
        }


        public void setInteractionType(String interactionType) {
            this.interactionType = interactionType;
        }
    }
}
