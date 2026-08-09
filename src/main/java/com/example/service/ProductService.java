package com.example.demo.service;

import com.example.demo.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
@Service
@Slf4j
public class ProductService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 模拟产品数据库（内存存储）
    private static final Map<Long, Product> productStore = new HashMap<>();

    static {
        // 初始化产品数据
        productStore.put(1L, new Product(1L, "Laptop", 999.99, 10));
        productStore.put(2L, new Product(2L, "Mouse", 29.99, 50));
        productStore.put(3L, new Product(3L, "Keyboard", 79.99, 30));
        productStore.put(4L, new Product(4L, "Monitor", 299.99, 15));
        productStore.put(5L, new Product(5L, "Headset", 149.99, 25));
    }
    public List<Product> getAllProducts() {
        log.info("Fetching all products from in-memory store");
        return new ArrayList<>(productStore.values());
    }
    
