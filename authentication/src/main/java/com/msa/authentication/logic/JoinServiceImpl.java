package com.msa.authentication.logic;

import com.msa.authentication.dto.request.JoinRequest;
import com.msa.authentication.dto.response.JoinResponse;
import com.msa.authentication.entity.User;
import com.msa.authentication.inbound.port.JoinService;
import com.msa.authentication.outbound.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JoinServiceImpl implements JoinService {

    private final UserRepository userRepository;

    @Override
    public JoinResponse join(JoinRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String email = request.getEmail();
        String name = request.getName();
        String phone = request.getPhone();

        User user = User.builder()
                .username(username)
                .password(password)
                .email(email)
                .name(name)
                .phone(phone)
                .build();

        userRepository.save(user);

        return new JoinResponse(username);
    }
}
