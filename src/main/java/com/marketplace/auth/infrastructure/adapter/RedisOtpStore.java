package com.marketplace.auth.infrastructure.adapter;

import com.marketplace.auth.application.port.OtpStore;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;

@Component
public class RedisOtpStore implements OtpStore {

    private static final Duration TTL = Duration.ofMinutes(15);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redis;

    public RedisOtpStore(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public String generate(String email) {
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        redis.opsForValue().set(key(email), otp, TTL);
        return otp;
    }

    @Override
    public boolean verify(String email, String otp) {
        String stored = redis.opsForValue().get(key(email));
        if (stored != null && stored.equals(otp)) {
            redis.delete(key(email));
            return true;
        }
        return false;
    }

    private String key(String email) {
        return "otp:" + email;
    }
}
