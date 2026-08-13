package com.example.demo.service;

import com.example.demo.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GPTSearchService {

    @Autowired
    private LLMRateLimiter llmRateLimiter;

    @Autowired
    private ProductService productService;

    public List<Product> intelligentSearch(String userId, String userQuery) {
        log.info("Intelligent search from user: {}, query: {}", userId, userQuery);

        try {
            String extractPrompt = String.format(
                    "User query: \"%s\"\n\n" +
                            "Extract keywords (product names, features, etc.) from the query, separated by commas.\n" +
                            "Return only the keyword list, with no additional explanation.",
                    userQuery
            );

            String keywords = llmRateLimiter.askGPT(userId, extractPrompt);
            log.info("GPT extracted keywords: {}", keywords);

            List<Product> products = searchLocalProducts(keywords);

            if (products.isEmpty()) {
                log.info("No products found for query: {}", userQuery);
                return List.of();
            }

            String rankPrompt = buildRankingPrompt(userQuery, products);
            String ranking = llmRateLimiter.askGPT(userId, rankPrompt);

            log.info("GPT ranking result: {}", ranking);

            return rankProducts(products, ranking);

        } catch (Exception e) {
            log.error("Intelligent search failed", e);
            return productService.searchByName(userQuery);
        }
    }

    private List<Product> searchLocalProducts(String keywords) {
        String[] keywordArray = keywords.split(",");

        List<Product> allProducts = productService.getAllProducts();

        return allProducts.stream()
                .filter(product ->
                        java.util.Arrays.stream(keywordArray)
                                .anyMatch(kw ->
                                        product.getName().toLowerCase()
                                                .contains(kw.trim().toLowerCase())
                                )
                )
                .limit(5)
                .collect(Collectors.toList());
    }

    private String buildRankingPrompt(String userQuery, List<Product> products) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("User query: \"").append(userQuery).append("\"\n\n");
        prompt.append("Available products:\n");

        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            prompt.append(i + 1).append(". ").append(p.getName())
                    .append(" ($").append(p.getPrice()).append(")\n");
        }

        prompt.append("\nPlease rank the products based on the user query and briefly state your rationale.");

        return prompt.toString();
    }

    private List<Product> rankProducts(List<Product> products, String gptRanking) {
        return products;
    }
}
