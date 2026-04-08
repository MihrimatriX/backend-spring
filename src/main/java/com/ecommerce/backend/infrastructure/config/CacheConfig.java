package com.ecommerce.backend.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    @Configuration
    @ConditionalOnProperty(prefix = "app.integration", name = "redis", havingValue = "true", matchIfMissing = true)
    static class RedisCacheBeans {

        @Value("${spring.data.redis.host:${spring.redis.host:localhost}}")
        private String redisHost;

        @Value("${spring.data.redis.port:${spring.redis.port:6379}}")
        private int redisPort;

        @Value("${spring.data.redis.password:${spring.redis.password:}}")
        private String redisPassword;

        @Bean
        public GenericJackson2JsonRedisSerializer redisJsonSerializer() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            return new GenericJackson2JsonRedisSerializer(mapper);
        }

        @Bean
        public RedisConnectionFactory redisConnectionFactory() {
            RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration(redisHost, redisPort);
            if (StringUtils.hasText(redisPassword)) {
                standalone.setPassword(RedisPassword.of(redisPassword));
            }
            LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone);
            log.info("Redis connection factory configured for host: {}, port: {}", redisHost, redisPort);
            return factory;
        }

        @Bean
        public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory,
                GenericJackson2JsonRedisSerializer redisJsonSerializer) {
            RedisTemplate<String, Object> template = new RedisTemplate<>();
            template.setConnectionFactory(connectionFactory);
            template.setKeySerializer(new StringRedisSerializer());
            template.setHashKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(redisJsonSerializer);
            template.setHashValueSerializer(redisJsonSerializer);
            template.afterPropertiesSet();
            log.info("Redis template configured with JSON serialization");
            return template;
        }

        @Bean
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                GenericJackson2JsonRedisSerializer redisJsonSerializer) {
            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(30))
                    .serializeKeysWith(
                            org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                                    .fromSerializer(new StringRedisSerializer()))
                    .serializeValuesWith(
                            org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                                    .fromSerializer(redisJsonSerializer));

            RedisCacheManager cacheManager = RedisCacheManager.builder(connectionFactory)
                    .cacheDefaults(config)
                    .withCacheConfiguration("products", config.entryTtl(Duration.ofMinutes(15)))
                    .withCacheConfiguration("categories", config.entryTtl(Duration.ofHours(1)))
                    .withCacheConfiguration("users", config.entryTtl(Duration.ofMinutes(10)))
                    .withCacheConfiguration("orders", config.entryTtl(Duration.ofMinutes(5)))
                    .build();

            log.info("Redis cache manager configured with default TTL: 30 minutes");
            return cacheManager;
        }
    }

    @Configuration
    @ConditionalOnProperty(prefix = "app.integration", name = "redis", havingValue = "false")
    static class LocalCacheBeans {

        @Bean
        public CacheManager cacheManager() {
            log.info("Using in-memory ConcurrentMapCacheManager (Redis disabled for this profile)");
            return new ConcurrentMapCacheManager("products", "categories", "users", "orders");
        }
    }
}
