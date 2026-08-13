package com.example.demo.service;

import com.example.demo.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CheckoutAssistantService {

    @Autowired
    private LLMRateLimiter llmRateLimiter;

    @Autowired
    private ProductService productService;

    public List<Product> getCheckoutSuggestions(String userId, List<Product> cartItems) {
        log.info("Getting checkout suggestions for user: {}, items: {}",
                userId, cartItems.size());

        try {
            String suggestionPrompt = buildSuggestionPrompt(cartItems);
            String suggestions = llmRateLimiter.askGPT(userId, suggestionPrompt);

            log.info("GPT suggestions: {}", suggestions);

            List<Product> recommendedProducts = new ArrayList<>();

            String[] suggestedNames = extractProductNames(suggestions);

            for (String productName : suggestedNames) {
                List<Product> found = productService.searchByName(productName);
                if (!found.isEmpty()) {
                    recommendedProducts.add(found.get(0));
                }
            }

            return recommendedProducts.stream()
                    .limit(3)
                    .toList();

        } catch (Exception e) {
            log.error("Checkout assistant failed", e);
            return getDefaultAccessories();
        }
    }

    private String buildSuggestionPrompt(List<Product> cartItems) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Products in user cart:\n");

        for (Product item : cartItems) {
            prompt.append("- ").append(item.getName())
                    .append(" ($").append(item.getPrice()).append(")\n");
        }

        prompt.append("\nPlease suggest 3 related accessories or add-ons, separated by commas.\n")
                .append("Provide product names only, no additional text.\n")
                .append("Example: Screen Protector, Phone Case, Charger");

        return prompt.toString();
    }

    private String[] extractProductNames(String response) {
        return response.split(",");
    }

    private List<Product> getDefaultAccessories() {
        List<Product> accessories = new ArrayList<>();

        accessories.add(new Product(null, "Screen Protector", 29.99, 100));
        accessories.add(new Product(null, "Phone Case", 49.99, 100));
        accessories.add(new Product(null, "Fast Charging Cable", 39.99, 100));

        return accessories;
    }

    public Map<String, Object> getDetailedSuggestions(String userId, List<Product> cartItems) {
        List<Product> suggestions = getCheckoutSuggestions(userId, cartItems);

        double originalTotal = cartItems.stream()
                .mapToDouble(Product::getPrice)
                .sum();

        double additionalTotal = suggestions.stream()
                .mapToDouble(Product::getPrice)
                .sum();

        return Map.of(
                "cart_total", originalTotal,
                "suggested_items", suggestions,
                "additional_cost", additionalTotal,
                "new_total", originalTotal + additionalTotal,
                "savings_estimate", "Recommended accessories enhance your overall setup"
        );
    }
}
