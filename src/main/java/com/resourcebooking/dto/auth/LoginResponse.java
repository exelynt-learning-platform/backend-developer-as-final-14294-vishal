package com.resourcebooking.dto.auth;

public record LoginResponse(

        String token,
        String tokenType,
        long expiresIn
) {
}