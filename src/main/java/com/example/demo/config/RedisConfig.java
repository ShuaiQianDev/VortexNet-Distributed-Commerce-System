package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis Configuration
 *
 * This class configures RedisTemplate to work with String key-value pairs.
 * It sets up the serialization strategy for Redis operations.
 *
 * Key points:
 * - StringRedisSerializer: Converts data to/from strings
 * - RedisTemplate: Main interface for Redis operations
 * - Connection pooling is configured in application.properties
 */
@Configuration
public class RedisConfig {

    /**
     * Configure RedisTemplate for String key-value operations
     *
     * @param factory RedisConnectionFactory provided by Spring Boot
     * @return Configured RedisTemplate bean
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {

        // Create a new RedisTemplate instance
        RedisTemplate<String, String> template = new RedisTemplate<>();

        // Set the connection factory
        template.setConnectionFactory(factory);

        // Create a String serializer for converting data to/from strings
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // Set serializer for keys (ensures keys are stored as strings)
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // Set serializer for values (ensures values are stored as strings)
        template.setValueSerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);

        // Initialize the template after setting serializers
        template.afterPropertiesSet();

        return template;
    }
}
