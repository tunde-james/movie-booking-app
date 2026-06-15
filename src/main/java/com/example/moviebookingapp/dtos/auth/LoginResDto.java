package com.example.moviebookingapp.dtos.auth;

public record LoginResDto(String accessToken, String tokenType, Long expiresInSeconds) {}
