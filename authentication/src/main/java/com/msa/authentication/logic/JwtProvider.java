package com.msa.authentication.logic;

import com.msa.authentication.dto.response.AuthResult;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private static final String SECRET_KEY = "secret-key";
    private static final long ACCESS_EXPIRATION = 1000 * 60 * 60;
    private static final long REFRESH_EXPIRATION = 1000 * 60 * 60 * 24;

    private final RedisTemplate<String, String> redisTemplate;

    public String generateAccessToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_EXPIRATION);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setId(jti)
                .claim("type", "access")
                .setExpiration(expiryDate)
                .signWith(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_EXPIRATION);
        String jti = UUID.randomUUID().toString();

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setId(jti)
                .claim("type", "refresh")
                .setExpiration(expiryDate)
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
                String blacklistKey = "blacklist:refresh:" + jti;
                if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
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

    public Date getAccessExpiration(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }

    public Date getRefreshExpiration(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET_KEY.getBytes()))
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getExpiration();
    }
}
