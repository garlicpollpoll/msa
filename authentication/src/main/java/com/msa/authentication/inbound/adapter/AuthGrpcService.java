package com.msa.authentication.inbound.adapter;

import auth.Auth;
import auth.AuthServiceGrpc;
import com.msa.authentication.dto.response.AuthResult;
import com.msa.authentication.dto.response.TokenResponse;
import com.msa.authentication.dto.response.UserResponse;
import com.msa.authentication.inbound.port.AuthService;
import com.msa.authentication.inbound.port.UserService;
import com.msa.authentication.logic.JwtProvider;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {

    private final AuthService authService;
    private final UserService userService;

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

    @Override
    public void getUserByUsername(Auth.GetUserRequest request, StreamObserver<Auth.GetUserResponse> responseObserver) {
        String username = request.getUsername();

        UserResponse userResponse = userService.findByUsername(username);

        Auth.GetUserResponse response = Auth.GetUserResponse.newBuilder()
                .setUsername(userResponse.getUsername())
                .setName(userResponse.getName())
                .setEmail(userResponse.getEmail())
                .setPhone(userResponse.getPhone())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
