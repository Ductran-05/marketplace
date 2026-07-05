package com.marketplace.auth.infrastructure.adapter;

import com.marketplace.auth.application.port.RefreshTokenStore;
import com.marketplace.auth.domain.model.UserId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RedisRefreshTokenStore implements RefreshTokenStore {

    private final StringRedisTemplate redis;
    private final Duration ttl;

    public RedisRefreshTokenStore(StringRedisTemplate redis,
                                  @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiryMs) {
        this.redis = redis;
        this.ttl = Duration.ofMillis(refreshTokenExpiryMs);
    }

    @Override
    public void save(UserId userId, String token) {
        redis.opsForValue().set(key(userId), token, ttl);
    }

    @Override
    public boolean matches(UserId userId, String token) {
        String stored = redis.opsForValue().get(key(userId));
        return stored != null && stored.equals(token);
    }

    @Override
    public void revoke(UserId userId) {
        redis.delete(key(userId));
    }

    private String key(UserId userId) {
        return "refresh:" + userId.value();
    }
}
