package com.msa.authentication.outbound.adapter;

import com.msa.authentication.entity.User;
import com.msa.authentication.outbound.port.UserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaUserAdapter implements UserPort {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public Optional<User> findByUsername(String username) {
        return springDataUserRepository.findByUsername(username);
    }

} 