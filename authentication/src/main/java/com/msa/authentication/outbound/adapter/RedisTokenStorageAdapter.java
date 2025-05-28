package com.msa.authentication.outbound.adapter;

import com.msa.authentication.outbound.port.TokenStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisTokenStorageAdapter implements TokenStorage {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void saveRefreshToken(String username, String token, Duration expiry) {
        String key = "refresh:" + username;
        redisTemplate.opsForValue().set(key, token, expiry.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public Optional<String> getRefreshToken(String username) {
        String key = "refresh:" + username;
        String token = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(token);
    }

    @Override
    public void tokenToBlacklist(String jti, Duration expiry) {
        String key = "blacklist:refresh:" + jti;
        redisTemplate.opsForValue().set(key, "blacklisted", expiry.toMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isBlacklisted(String jti) {
        String key = "blacklist:refresh:" + jti;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void removeRefreshToken(String username) {
        String key = "refresh:" + username;
        redisTemplate.delete(key);
    }
} 