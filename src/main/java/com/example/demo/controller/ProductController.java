package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/products")
@Slf4j
public class ProductController {

    @Autowired
    private ProductService productService;
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        log.info("GET /api/products - Fetching all products");

        try {
            List<Product> products = productService.getAllProducts();

            return ResponseEntity.ok()
                    .body(new ApiResponse("success", "Products retrieved successfully", products));
        } catch (Exception e) {
            log.error("Error fetching products", e);
            return ResponseEntity.status(500)
                    .body(new ApiResponse("error", e.getMessage(), null));
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        log.info("GET /api/products/{} - Fetching product", id);

        long startTime = System.currentTimeMillis();

        try {
            Product product = productService.getProductById(id);

            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            log.info("Product fetched in {}ms", duration);

            return ResponseEntity.ok()
                    .header("X-Response-Time", duration + "ms")
                    .body(new ApiResponse("success", "Product retrieved successfully", product));
        } catch (Exception e) {
            log.error("Error fetching product", e);
            return ResponseEntity.status(404)
                    .body(new ApiResponse("error", e.getMessage(), null));
        }
    }
