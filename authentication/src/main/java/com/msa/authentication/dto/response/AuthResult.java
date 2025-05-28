package com.msa.authentication.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResult {

    private boolean valid;
    private String username;
    private String jti;
}
