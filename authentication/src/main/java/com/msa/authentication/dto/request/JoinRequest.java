package com.msa.authentication.dto.request;

import lombok.Data;

@Data
public class JoinRequest {

    private String username;
    private String password;
    private String name;
    private String email;
    private String phone;
}
