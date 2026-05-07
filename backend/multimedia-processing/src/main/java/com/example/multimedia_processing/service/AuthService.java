package com.example.multimedia_processing.service;

import com.example.multimedia_processing.dto.RegisterRequest;
import com.example.multimedia_processing.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {

        if (userService.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User already exists");
        }

        User user = new User();

        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        user.setRole("USER");

        userService.save(user);
    }
}
