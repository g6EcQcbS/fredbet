package de.fred4jupiter.fredbet.config;

import de.fred4jupiter.fredbet.props.CacheNames;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final String[] CACHE_NAMES = {
        CacheNames.AVAIL_GROUPS,
        CacheNames.CHILD_RELATION,
        CacheNames.RUNTIME_SETTINGS,
        CacheNames.POINTS_CONFIG,
        CacheNames.FOOTBALL_DATA_SETTINGS
    };

    /**
     * Redis-backed distributed cache — used when Redis is available (docker profile).
     * Ensures cache consistency across multiple application instances.
     */
    @Bean
    @ConditionalOnProperty("spring.data.redis.host")
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        JdkSerializationRedisSerializer serializer = new JdkSerializationRedisSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(60))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(serializer)
            );

        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(Map.of(
                CacheNames.RUNTIME_SETTINGS,       defaultConfig.entryTtl(Duration.ofMinutes(30)),
                CacheNames.CHILD_RELATION,         defaultConfig.entryTtl(Duration.ofMinutes(60)),
                CacheNames.AVAIL_GROUPS,           defaultConfig.entryTtl(Duration.ofMinutes(60)),
                CacheNames.POINTS_CONFIG,          defaultConfig.entryTtl(Duration.ofHours(24)),
                CacheNames.FOOTBALL_DATA_SETTINGS, defaultConfig.entryTtl(Duration.ofHours(24))
            ))
            .build();
    }

    /**
     * Fallback in-memory cache — used for local development without Redis.
     */
    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager inMemoryCacheManager() {
        return new ConcurrentMapCacheManager(CACHE_NAMES);
    }

}
