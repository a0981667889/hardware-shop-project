package com.example.starter.dto;

import com.example.starter.security.UserPrincipal;

import java.util.List;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    Long userId,
    String username,
    List<String> roles
) {
    public static AuthResponse of(String accessToken, String refreshToken,
                                  UserPrincipal principal) {
        return new AuthResponse(
            accessToken, refreshToken, "Bearer",
            principal.getId(), principal.getUsername(), principal.getRoleNames()
        );
    }
}
