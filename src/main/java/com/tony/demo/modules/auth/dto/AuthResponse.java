package com.tony.demo.modules.auth.dto;

public record AuthResponse(
    String token,
    String username,
    String message,
    String role
) {}
