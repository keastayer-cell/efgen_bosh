package com.efgenbosh.backend.dto.auth;

import java.util.List;

public record AuthResponse(
    String token,
    String tokenType,
    Long userId,
    String email,
    String name,
    List<String> roles,
    boolean mustChangePassword
) {
    public AuthResponse(
        String token,
        Long userId,
        String email,
        String name,
        List<String> roles,
        boolean mustChangePassword
    ) {
        this(token, "Bearer", userId, email, name, roles, mustChangePassword);
    }
}
