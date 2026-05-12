package com.example.multimedia_processing.controller;

import com.example.multimedia_processing.dto.AuthResponse;
import com.example.multimedia_processing.dto.LoginRequest;
import com.example.multimedia_processing.dto.RegisterRequest;
import com.example.multimedia_processing.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody RegisterRequest request) {
        authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
