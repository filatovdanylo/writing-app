package com.calmlywriter.dto;

public record AuthResponse(String token, Long userId, String email) {
}
