package com.msa.authentication.inbound.port;

import com.msa.authentication.dto.response.AuthResult;
import com.msa.authentication.dto.response.TokenResponse;

public interface AuthService {

    TokenResponse login(String username, String password);
    AuthResult verifyToken(String token);
    TokenResponse refreshToken(String refreshToken);
    TokenResponse autoRefreshToken(String accessToken, String refreshToken);
}
