package com.togetherly.demo.config;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;

/**
 * Configures how data is serialized when stored in Redis.
 *
 * Keys are serialized as plain strings; values as JSON.
 * This makes Redis data human-readable (you can inspect it with redis-cli).
 *
 * Spring Data Redis 4.0 uses Jackson 3's GenericJacksonJsonRedisSerializer
 * (replaces the deprecated GenericJackson2JsonRedisSerializer from Jackson 2).
 */
@Configuration
public class RedisConfig {
    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    @Bean
    public RedisTemplate<String, Map<String, Object>> hashRedisTemplate() {
        RedisTemplate<String, Map<String, Object>> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        GenericJacksonJsonRedisSerializer jsonSerializer =
                new GenericJacksonJsonRedisSerializer(new ObjectMapper());

        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jsonSerializer);
        redisTemplate.setHashValueSerializer(jsonSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
