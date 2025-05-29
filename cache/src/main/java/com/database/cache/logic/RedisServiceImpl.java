package com.database.cache.logic;

import com.database.cache.domain.CacheDomain;
import com.database.cache.dto.CacheType;
import com.database.cache.dto.ResponseDto;
import com.database.cache.inbound.port.RedisService;
import com.database.cache.outbound.port.CacheStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final CacheStorage cacheStorage;

    @Override
    public ResponseDto<?> saveKey(String key, String value) {
        try {
            cacheStorage.save(key, value);

            return new ResponseDto<>(true, "success");
        }
        catch (Exception e) {
            return new ResponseDto<>(false, e.getMessage());
        }
    }

    // TODO 주의깊게 볼 것
    @Override
    public ResponseDto<?> getValue(String key, String typeStr) {
        try {
            CacheType type = CacheType.fromString(typeStr);
            Object cached = cacheStorage.get(key);

            if (cached == null) {
                return new ResponseDto<>(false, null);
            }

            return switch (type) {
                case NORMAL -> {
                    if (cached instanceof CacheDomain.ForNormal normal) {
                        yield new ResponseDto<>(true, normal);
                    } else {
                        yield new ResponseDto<>(false, "Type mismatch");
                    }
                }
                case AUTH -> {
                    if (cached instanceof CacheDomain.ForAuthentication auth) {
                        yield new ResponseDto<>(true, auth);
                    } else {
                        yield new ResponseDto<>(false, "Type mismatch");
                    }
                }
            };
        } catch (Exception e) {
            return new ResponseDto<>(false, e.getMessage());
        }
    }

    @Override
    public boolean hasKey(String key) {
        return cacheStorage.hasKey(key);
    }
}
