package com.resourcebooking.service;

import com.resourcebooking.dto.auth.LoginRequest;
import com.resourcebooking.dto.auth.LoginResponse;
import com.resourcebooking.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.username(),
                                request.password()
                        )
                );

        String username = authentication.getName();

        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(authority ->
                        authority.getAuthority()
                                .replace("ROLE_", "")
                )
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Authenticated user has no role"
                        )
                );

        String token = jwtService.generateToken(
                username,
                role
        );

        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpiration()
        );
    }
}