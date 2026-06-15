package com.example.moviebookingapp.dtos.auth;

public record RegisterResDto(
        Long id,
        String username,
        String email,
        String role,
        String accessToken,
        String tokenType,
        Long expiresInSeconds) {}
