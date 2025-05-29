package com.database.cache.domain;

import lombok.Getter;

import java.time.Duration;
import java.util.Date;

public class CacheDomain {

    @Getter
    public static class ForNormal {
        private final String key;
        private final String value;
        // TODO 추후 주문, 결제 등에서 엔티티의 포맷이 정해지면 그 때 바꿀 예정

        public ForNormal(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    @Getter
    public static class ForAuthentication {
        private final String subject;
        private final Duration expiration;
        private final Date issuedAt;
        private final String jti;
        private final ForNormal baseCache;

        public ForAuthentication(String subject, Duration expiration, Date issuedAt, String jti, ForNormal baseCache) {
            this.subject = subject;
            this.expiration = expiration;
            this.issuedAt = issuedAt;
            this.jti = jti;
            this.baseCache = baseCache;
        }
    }
}
