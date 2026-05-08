package com.robintech.backend.service;


import com.robintech.backend.dto.AuthResponse;
import com.robintech.backend.dto.LoginRequest;
import com.robintech.backend.dto.RegisterRequest;
import com.robintech.backend.model.User;
import com.robintech.backend.repository.UserRepository;
import com.robintech.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.core.ParameterizedTypeReference;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${github.client.id}")
    private String githubClientId;

    @Value("${github.client.secret}")
    private String githubClientSecret;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email already in use");
        if (userRepository.existsByUsername(request.getUsername()))
            throw new RuntimeException("Username already taken");

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .userId(user.getId())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        String token = jwtUtil.generateToken(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .userId(user.getId())
                .build();
    }

    public AuthResponse loginWithGithub(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // 1. Exchange code for access token
        String tokenUrl = "https://github.com/login/oauth/access_token";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));
        
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", githubClientId);
        body.add("client_secret", githubClientSecret);
        body.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
                tokenUrl, HttpMethod.POST, request, new ParameterizedTypeReference<Map<String, Object>>() {});
        
        if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null || tokenResponse.getBody().containsKey("error")) {
            throw new RuntimeException("Failed to authenticate with GitHub");
        }
        
        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // 2. Fetch user profile
        String userUrl = "https://api.github.com/user";
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<String> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                userUrl, HttpMethod.GET, userRequest, new ParameterizedTypeReference<Map<String, Object>>() {});
        
        if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
            throw new RuntimeException("Failed to fetch GitHub profile");
        }
        
        Map<String, Object> userData = userResponse.getBody();
        String githubLogin = (String) userData.get("login");
        String email = (String) userData.get("email");
        String avatarUrl = (String) userData.get("avatar_url");
        String githubUrl = (String) userData.get("html_url");
        
        // If email is private, fetch from /user/emails
        if (email == null) {
            String emailsUrl = "https://api.github.com/user/emails";
            ResponseEntity<java.util.List<Map<String, Object>>> emailsResponse = restTemplate.exchange(
                    emailsUrl, HttpMethod.GET, userRequest, new ParameterizedTypeReference<java.util.List<Map<String, Object>>>() {});
            if (emailsResponse.getStatusCode().is2xxSuccessful() && emailsResponse.getBody() != null) {
                for (Map<String, Object> emailObj : emailsResponse.getBody()) {
                    if (Boolean.TRUE.equals(emailObj.get("primary"))) {
                        email = (String) emailObj.get("email");
                        break;
                    }
                }
            }
        }
        
        if (email == null) {
             email = githubLogin + "@github.com"; // Fallback
        }

        // 3. Find or Create User
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            // Check by username just in case
            if (userRepository.existsByUsername(githubLogin)) {
                githubLogin = githubLogin + "_" + UUID.randomUUID().toString().substring(0, 4);
            }
            user = User.builder()
                    .username(githubLogin)
                    .email(email)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString())) // Random password
                    .avatarUrl(avatarUrl)
                    .githubUrl(githubUrl)
                    .build();
            user = userRepository.save(user);
        } else {
            // Update github profile info if empty
            boolean changed = false;
            if (user.getGithubUrl() == null || user.getGithubUrl().isEmpty()) {
                user.setGithubUrl(githubUrl);
                changed = true;
            }
            if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
                user.setAvatarUrl(avatarUrl);
                changed = true;
            }
            if (changed) {
                userRepository.save(user);
            }
        }

        String token = jwtUtil.generateToken(user.getEmail());
        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .userId(user.getId())
                .build();
    }
}