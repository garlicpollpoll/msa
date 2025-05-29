package com.database.cache.outbound.port;

import com.database.cache.domain.CacheDomain;
import com.database.cache.dto.ResponseDto;

public interface CacheStorage {

    void save(String key, Object value);
    Object get(String key);
    boolean hasKey(String key);
}
