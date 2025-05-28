package com.msa.authentication.logic;

import com.msa.authentication.dto.response.AuthResult;
import com.msa.authentication.outbound.port.TokenStorage;
import com.msa.authentication.domain.AuthenticationDomain;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private static final String SECRET_KEY = "secret-key";
    private static final long ACCESS_EXPIRATION = 1000 * 60 * 60;
    private static final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7;

    private final TokenStorage tokenStorage;

    public String generateAccessToken(String username) {
        AuthenticationDomain.TokenInfo tokenInfo = createTokenInfo(username, "access", ACCESS_EXPIRATION);
        return buildJwtToken(tokenInfo);
    }

    public String generateRefreshToken(String username) {
        AuthenticationDomain.TokenInfo tokenInfo = createTokenInfo(username, "refresh", REFRESH_EXPIRATION);
        String token = buildJwtToken(tokenInfo);

        tokenStorage.saveRefreshToken(username, token, Duration.ofMillis(REFRESH_EXPIRATION));
        
        return token;
    }

    private AuthenticationDomain.TokenInfo createTokenInfo(String username, String type, long expirationMs) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMs);
        String jti = UUID.randomUUID().toString();
        
        return new AuthenticationDomain.TokenInfo(username, jti, now, expiration, type);
    }

    private String buildJwtToken(AuthenticationDomain.TokenInfo tokenInfo) {
        return Jwts.builder()
                .setSubject(tokenInfo.getUsername())
                .setIssuedAt(tokenInfo.getIssuedAt())
                .setId(tokenInfo.getJti())
                .claim("type", tokenInfo.getType())
                .setExpiration(tokenInfo.getExpiration())
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public AuthResult validateToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String jti = claims.getId();
            String subject = claims.getSubject();
            String tokenType = claims.get("type", String.class);

            if ("refresh".equals(tokenType)) {
                if (tokenStorage.isBlacklisted(jti)) {
                    return new AuthResult(false, subject, jti);
                }
            }

            return new AuthResult(true, subject, jti);
        }
        catch (Exception e) {
            return new AuthResult(false, null, null);
        }
    }

    public String getTokenType(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            
            return claims.get("type", String.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Date getTokenExpiration(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration();
        } catch (Exception e) {
            return null;
        }
    }

    public void tokenToBlacklist(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String jti = claims.getId();
            Date expiration = claims.getExpiration();
            long remainingTime = expiration.getTime() - System.currentTimeMillis();
            
            if (remainingTime > 0) {
                tokenStorage.tokenToBlacklist(jti, Duration.ofMillis(remainingTime));
            }
        } catch (Exception e) {
            // 토큰이 유효하지 않으면 블랙리스트에 추가할 필요 없음
        }
    }
}
