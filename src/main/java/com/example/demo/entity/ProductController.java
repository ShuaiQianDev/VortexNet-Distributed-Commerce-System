package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST Controller for Product API
 *
 * Endpoints:
 * - GET    /api/products           → Get all products
 * - GET    /api/products/{id}      → Get product by ID
 * - POST   /api/products           → Create new product
 * - PUT    /api/products/{id}      → Update product
 * - DELETE /api/products/{id}      → Delete product
 *
 * @author Alex Quinn
 * @version 1.0
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Get all products
     *
     * @http GET /api/products
     * @response 200 OK with product list
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    /**
     * Get product by ID
     *
     * @http GET /api/products/{id}
     * @param id the product ID
     * @response 200 OK if found, 404 Not Found if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);

        if (product != null) {
            return new ResponseEntity<>(product, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Product with ID " + id + " not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Create new product
     *
     * @http POST /api/products
     * @param product the product data from request body
     * @response 201 Created with created product
     */
    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody Product product) {
        try {
            Product createdProduct = productService.createProduct(product);
            return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Server error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update existing product
     *
     * @http PUT /api/products/{id}
     * @param id the product ID to update
     * @param productDetails the new product data
     * @response 200 OK if updated, 404 Not Found if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Product updatedProduct = productService.updateProduct(id, productDetails);

        if (updatedProduct != null) {
            return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Product with ID " + id + " not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Delete product by ID
     *
     * @http DELETE /api/products/{id}
     * @param id the product ID to delete
     * @response 204 No Content if deleted, 404 Not Found if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);

        if (deleted) {
            return new ResponseEntity<>("Product deleted successfully", HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>("Product with ID " + id + " not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Search products by name
     *
     * @http GET /api/products/search?keyword=xxx
     * @param keyword search keyword
     * @response 200 OK with matching products
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchByName(@RequestParam String keyword) {
        List<Product> results = productService.searchProductByName(keyword);
        return new ResponseEntity<>(results, HttpStatus.OK);
    }
}
