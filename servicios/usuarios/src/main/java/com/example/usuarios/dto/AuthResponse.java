package com.example.usuarios.dto;

public record AuthResponse(String accessToken, long expiresIn, String tokenType) {
    public AuthResponse(String accessToken, long expiresIn) {
        this(accessToken, expiresIn, "Bearer");
    }
}
