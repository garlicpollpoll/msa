package com.msa.authentication.logic;

import com.msa.authentication.dto.response.UserResponse;
import com.msa.authentication.entity.User;
import com.msa.authentication.inbound.port.UserService;
import com.msa.authentication.outbound.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResponse findByUsername(String username) {
        User findUser = userRepository.findByUsername(username).orElseThrow(() -> new IllegalStateException("User not found"));

        return UserResponse.builder()
                .username(findUser.getUsername())
                .name(findUser.getName())
                .email(findUser.getEmail())
                .phone(findUser.getPhone())
                .build();
    }
}
