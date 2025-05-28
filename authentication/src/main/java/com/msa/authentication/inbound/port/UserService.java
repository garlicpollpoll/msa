package com.msa.authentication.inbound.port;

import com.msa.authentication.dto.response.UserResponse;

public interface UserService {

    UserResponse findByUsername(String username);
}
