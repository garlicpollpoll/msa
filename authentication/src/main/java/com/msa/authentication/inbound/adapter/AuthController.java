package com.msa.authentication.inbound.adapter;

import com.msa.authentication.dto.request.LoginRequest;
import com.msa.authentication.dto.response.TokenResponse;
import com.msa.authentication.inbound.port.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        TokenResponse token = authService.login(username, password);

        return ResponseEntity.ok(token);
    }

}
