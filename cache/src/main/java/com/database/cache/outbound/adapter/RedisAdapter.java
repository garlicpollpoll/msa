package com.database.cache.outbound.adapter;

import com.database.cache.domain.CacheDomain;
import com.database.cache.dto.ResponseDto;
import com.database.cache.outbound.port.CacheStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisAdapter implements CacheStorage {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
