package com.msa.authentication.outbound.port;

import com.msa.authentication.entity.User;
import java.util.Optional;

public interface UserPort {
    Optional<User> findByUsername(String username);
} 