package com.database.cache.dto;

import java.util.Arrays;

public enum CacheType {
    NORMAL("normal"),
    AUTH("auth");

    private final String value;

    CacheType(String value) {
        this.value = value;
    }

    public static CacheType fromString(String type) {
        return Arrays.stream(values())
                .filter(t -> t.value.equals(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid cache type: " + type));
    }
}
