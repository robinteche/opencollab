package com.robintech.backend.controller;


import com.robintech.backend.dto.AuthResponse;
import com.robintech.backend.dto.GithubLoginRequest;
import com.robintech.backend.dto.LoginRequest;
import com.robintech.backend.dto.RegisterRequest;
import com.robintech.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/github")
    public ResponseEntity<AuthResponse> githubLogin(@Valid @RequestBody GithubLoginRequest request) {
        return ResponseEntity.ok(authService.loginWithGithub(request.getCode()));
    }
}
