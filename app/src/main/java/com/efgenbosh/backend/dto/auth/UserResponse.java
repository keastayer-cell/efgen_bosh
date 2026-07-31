package com.efgenbosh.backend.dto.auth;

import java.util.List;

public record UserResponse(
    Long id,
    String email,
    String name,
    List<String> roles,
    boolean mustChangePassword
) {
}
