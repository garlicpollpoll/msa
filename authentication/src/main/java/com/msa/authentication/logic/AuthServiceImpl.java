package com.msa.authentication.logic;

import com.msa.authentication.dto.response.AuthResult;
import com.msa.authentication.dto.response.TokenResponse;
import com.msa.authentication.entity.User;
import com.msa.authentication.inbound.port.AuthService;
import com.msa.authentication.outbound.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public TokenResponse login(String username, String password) {
        User findUser = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!bCryptPasswordEncoder.matches(password, findUser.getPassword())) {
            throw new IllegalArgumentException("Wrong password");
        }

        String accessToken = jwtProvider.generateAccessToken(username);
        String refreshToken = jwtProvider.generateRefreshToken(username);

        String redisKey = "refresh:" + username;
        long refreshTokenExpireMs = 1000 * 60 * 60 * 24 * 7;
        redisTemplate.opsForValue().set(redisKey, refreshToken, refreshTokenExpireMs, TimeUnit.MILLISECONDS);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResult verifyToken(String token) {
        String tokenType = jwtProvider.getTokenType(token);
        
        if ("access".equals(tokenType)) {
            AuthResult accessResult = jwtProvider.validateToken(token);
            if (accessResult.isValid()) {
                return accessResult;
            }
            return new AuthResult(false, accessResult.getUsername(), accessResult.getJti());
        }
        
        if ("refresh".equals(tokenType)) {
            AuthResult refreshResult = jwtProvider.validateToken(token);
            if (refreshResult.isValid()) {
                return refreshResult;
            }
            
            String username = refreshResult.getUsername();
            String redisKey = "refresh:" + username;
            String savedToken = redisTemplate.opsForValue().get(redisKey);
            
            if (savedToken == null || !savedToken.equals(token)) {
                return new AuthResult(false, username, refreshResult.getJti());
            }
            
            return refreshResult;
        }
        
        return new AuthResult(false, null, null);
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        AuthResult authResult = verifyToken(refreshToken);
        
        if (!authResult.isValid()) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String username = authResult.getUsername();
        String oldJti = authResult.getJti();

        String refreshBlacklistKey = "blacklist:refresh:" + oldJti;
        long oldExpire = jwtProvider.getRefreshExpiration(refreshToken).getTime() - System.currentTimeMillis();
        redisTemplate.opsForValue().set(refreshBlacklistKey, "blacklisted", oldExpire, TimeUnit.MILLISECONDS);

        String newAccessToken = jwtProvider.generateAccessToken(username);
        String newRefreshToken = jwtProvider.generateRefreshToken(username);

        String redisKey = "refresh:" + username;
        long refreshTokenExpireMs = 1000 * 60 * 60 * 24 * 7;
        redisTemplate.opsForValue().set(redisKey, newRefreshToken, refreshTokenExpireMs, TimeUnit.MILLISECONDS);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    /**
     * 토큰을 자동으로 갱신하는 메서드
     * 액세스 토큰이 만료되면 리프레시 토큰으로 새로운 토큰들을 발급
     */
    public TokenResponse autoRefreshToken(String accessToken, String refreshToken) {
        AuthResult accessResult = verifyToken(accessToken);
        if (accessResult.isValid()) {
            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }

        try {
            return refreshToken(refreshToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("Both tokens are invalid. Please login again.");
        }
    }
}
