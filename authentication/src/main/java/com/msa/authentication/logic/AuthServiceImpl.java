package com.msa.authentication.logic;

import com.msa.authentication.domain.AuthenticationDomain;
import com.msa.authentication.dto.response.AuthResult;
import com.msa.authentication.dto.response.TokenResponse;
import com.msa.authentication.entity.User;
import com.msa.authentication.inbound.port.AuthService;
import com.msa.authentication.outbound.port.UserPort;
import com.msa.authentication.outbound.port.TokenStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserPort userPort;
    private final TokenStorage tokenStorage;
    private final JwtProvider jwtProvider;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationDomain authenticationDomain;

    @Override
    public TokenResponse login(String username, String password) {
        // 1. 사용자 조회
        Optional<User> userOptional = userPort.findByUsername(username);
        
        // 2. 도메인 로직을 통한 인증
        AuthenticationDomain.AuthenticationResult authResult = 
            authenticationDomain.authenticate(userOptional.orElse(null), password, bCryptPasswordEncoder);

        if (!authResult.isSuccess()) {
            throw new IllegalArgumentException(authResult.getErrorMessage());
        }

        // 3. 토큰 생성
        String accessToken = jwtProvider.generateAccessToken(username);
        String refreshToken = jwtProvider.generateRefreshToken(username);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthResult verifyToken(String token) {
        // 1. 토큰 타입 확인
        String tokenType = jwtProvider.getTokenType(token);
        
        if ("access".equals(tokenType)) {
            AuthResult accessResult = jwtProvider.validateToken(token);
            if (accessResult.isValid()) {
                return accessResult;
            }
            // 액세스 토큰이 만료되었거나 유효하지 않음
            return new AuthResult(false, accessResult.getUsername(), accessResult.getJti());
        }
        
        // 2. 리프레시 토큰인 경우 검증
        if ("refresh".equals(tokenType)) {
            AuthResult refreshResult = jwtProvider.validateToken(token);
            if (!refreshResult.isValid()) {
                return refreshResult;
            }
            
            // 3. 저장소에 있는 리프레시 토큰과 비교
            String username = refreshResult.getUsername();
            Optional<String> savedToken = tokenStorage.getRefreshToken(username);
            
            if (savedToken.isEmpty() || !savedToken.get().equals(token)) {
                return new AuthResult(false, username, refreshResult.getJti());
            }
            
            return refreshResult;
        }
        
        // 토큰 타입을 알 수 없거나 유효하지 않은 토큰
        return new AuthResult(false, null, null);
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        // 1. 리프레시 토큰 검증
        AuthResult authResult = verifyToken(refreshToken);
        
        if (!authResult.isValid()) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        String username = authResult.getUsername();

        // 2. 기존 리프레시 토큰 블랙리스트 처리
        jwtProvider.tokenToBlacklist(refreshToken);

        // 3. 새로운 토큰들 생성
        String newAccessToken = jwtProvider.generateAccessToken(username);
        String newRefreshToken = jwtProvider.generateRefreshToken(username);

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    public TokenResponse autoRefreshToken(String accessToken, String refreshToken) {
        // 1. 액세스 토큰 검증
        AuthResult accessResult = verifyToken(accessToken);
        if (accessResult.isValid()) {
            // 액세스 토큰이 유효하면 그대로 반환
            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }

        // 2. 액세스 토큰이 만료되었으면 리프레시 토큰으로 갱신
        try {
            return refreshToken(refreshToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("Both tokens are invalid. Please login again.");
        }
    }

    // 로그아웃 기능 추가
    public void logout(String refreshToken) {
        try {
            AuthResult result = jwtProvider.validateToken(refreshToken);
            if (result.isValid()) {
                String username = result.getUsername();
                // 리프레시 토큰을 저장소에서 제거
                tokenStorage.removeRefreshToken(username);
                // 리프레시 토큰을 블랙리스트에 추가
                jwtProvider.tokenToBlacklist(refreshToken);
            }
        } catch (Exception e) {
            // 이미 유효하지 않은 토큰이므로 무시
        }
    }
}
