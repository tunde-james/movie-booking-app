package com.example.moviebookingapp.dtos.user;

import com.example.moviebookingapp.enums.UserRole;

public record UserResDto(Long id, String username, String email, String phoneNumber, UserRole role) {}
