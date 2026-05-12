package com.example.multimedia_processing.security;

import com.example.multimedia_processing.entity.User;
import com.example.multimedia_processing.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    @NotNull
    @Override
    public UserDetails loadUserByUsername(@NotNull String email)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new CustomUserPrincipal(
                user.getEmail(),
                user.getPassword(),
                user.getId().toString(),
                List.of()
        );
    }
}
