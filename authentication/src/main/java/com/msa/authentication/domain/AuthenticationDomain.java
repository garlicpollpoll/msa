package com.msa.authentication.domain;

import com.msa.authentication.entity.User;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
public class AuthenticationDomain {

    public boolean verifyPassword(String rawPassword, String hashedPassword, BCryptPasswordEncoder encoder) {
        return encoder.matches(rawPassword, hashedPassword);
    }

    public TokenPair generateTokenPair(String username) {
        Date now = new Date();
        String accessJti = UUID.randomUUID().toString();
        String refreshJti = UUID.randomUUID().toString();

        return new TokenPair(
                createTokenInfo(username, accessJti, now, 1000 * 60 * 60, "access"),
                createTokenInfo(username, refreshJti, now, 1000 * 60 * 60 * 24 * 7, "refresh")
        );
    }

    private TokenInfo createTokenInfo(String username, String jti, Date issuedAt, long expirationMs, String type) {
        Date expiration = new Date(issuedAt.getTime() + expirationMs);
        return new TokenInfo(username, jti, issuedAt, expiration, type);
    }

    public AuthenticationResult authenticate(User user, String rawPassword, BCryptPasswordEncoder encoder) {
        if (user == null) {
            return AuthenticationResult.failure("User not found");
        }

        if (!user.isActive()) {
            return AuthenticationResult.failure("User is inactive");
        }

        if (!verifyPassword(rawPassword, user.getPassword(), encoder)) {
            return AuthenticationResult.failure("Wrong password");
        }

        return AuthenticationResult.success(user);
    }

    @Getter
    public static class TokenPair {
        private final TokenInfo accessToken;
        private final TokenInfo refreshToken;

        public TokenPair(TokenInfo accessToken, TokenInfo refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }

    }

    @Getter
    public static class TokenInfo {
        private final String username;
        private final String jti;
        private final Date issuedAt;
        private final Date expiration;
        private final String type;

        public TokenInfo(String username, String jti, Date issuedAt, Date expiration, String type) {
            this.username = username;
            this.jti = jti;
            this.issuedAt = issuedAt;
            this.expiration = expiration;
            this.type = type;
        }

    }

    @Getter
    public static class AuthenticationResult {
        private final boolean success;
        private final User user;
        private final String errorMessage;

        private AuthenticationResult(boolean success, User user, String errorMessage) {
            this.success = success;
            this.user = user;
            this.errorMessage = errorMessage;
        }

        public static AuthenticationResult success(User user) {
            return new AuthenticationResult(true, user, null);
        }

        public static AuthenticationResult failure(String errorMessage) {
            return new AuthenticationResult(false, null, errorMessage);
        }

    }
}