package com.msa.authentication.inbound.adapter;

import auth.Auth;
import auth.AuthServiceGrpc;
import com.msa.authentication.dto.response.AuthResult;
import com.msa.authentication.dto.response.TokenResponse;
import com.msa.authentication.inbound.port.AuthService;
import com.msa.authentication.logic.JwtProvider;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthService authService;
    private final JwtProvider jwtProvider;

    @Override
    public void verifyToken(Auth.TokenRequest request, StreamObserver<Auth.VerifyResponse> responseObserver) {
        String token = request.getToken();

        try {
            AuthResult result = authService.verifyToken(token);

            Auth.VerifyResponse response = Auth.VerifyResponse.newBuilder()
                    .setValid(result.isValid())
                    .setUsername(result.getUsername() == null ? "" : result.getUsername())
                    .setError("")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (Exception e) {
            Auth.VerifyResponse response = Auth.VerifyResponse.newBuilder()
                    .setValid(false)
                    .setUsername("")
                    .setError(e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void refreshToken(Auth.RefreshRequest request, StreamObserver<Auth.RefreshResponse> responseObserver) {
        try {
            TokenResponse tokenResponse = authService.refreshToken(request.getRefreshToken());
            Auth.RefreshResponse response = Auth.RefreshResponse.newBuilder()
                    .setAccessToken(tokenResponse.getAccessToken())
                    .setRefreshToken(tokenResponse.getRefreshToken())
                    .setSuccess(true)
                    .setError("")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        catch (Exception e) {
            Auth.RefreshResponse response = Auth.RefreshResponse.newBuilder()
                    .setAccessToken("")
                    .setRefreshToken("")
                    .setSuccess(false)
                    .setError(e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void autoVerifyAndRefresh(Auth.AutoRefreshRequest request, StreamObserver<Auth.AutoRefreshResponse> responseObserver) {
        try {
            String accessToken = request.getAccessToken();
            String refreshToken = request.getRefreshToken();

            if (accessToken.isEmpty()) {
                Auth.AutoRefreshResponse response = Auth.AutoRefreshResponse.newBuilder()
                        .setValid(false)
                        .setUsername("")
                        .setAccessToken("")
                        .setRefreshToken("")
                        .setRefreshed(false)
                        .setError("Access token is required")
                        .build();
                
                responseObserver.onNext(response);
                responseObserver.onCompleted();
                return;
            }

            TokenResponse result = authService.autoRefreshToken(accessToken, refreshToken);

            boolean refreshed = !accessToken.equals(result.getAccessToken());

            AuthResult authResult = authService.verifyToken(result.getAccessToken());
            
            Auth.AutoRefreshResponse response = Auth.AutoRefreshResponse.newBuilder()
                    .setValid(authResult.isValid())
                    .setUsername(authResult.getUsername() == null ? "" : authResult.getUsername())
                    .setAccessToken(result.getAccessToken())
                    .setRefreshToken(result.getRefreshToken())
                    .setRefreshed(refreshed)
                    .setError("")
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            Auth.AutoRefreshResponse response = Auth.AutoRefreshResponse.newBuilder()
                    .setValid(false)
                    .setUsername("")
                    .setAccessToken("")
                    .setRefreshToken("")
                    .setRefreshed(false)
                    .setError(e.getMessage())
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
