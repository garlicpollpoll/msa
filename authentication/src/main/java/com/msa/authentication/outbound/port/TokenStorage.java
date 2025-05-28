package com.msa.authentication.outbound.port;

import java.time.Duration;
import java.util.Optional;

public interface TokenStorage {
    void saveRefreshToken(String username, String token, Duration expiry);
    Optional<String> getRefreshToken(String username);
    void tokenToBlacklist(String jti, Duration expiry);
    boolean isBlacklisted(String jti);
    void removeRefreshToken(String username);
} 