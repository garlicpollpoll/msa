package com.database.cache.inbound.port;

import com.database.cache.domain.CacheDomain;
import com.database.cache.dto.ResponseDto;

public interface RedisService {

    ResponseDto<?> saveKey(String key, String value);
    ResponseDto<?> getValue(String key, String type);
    boolean hasKey(String key);
}
