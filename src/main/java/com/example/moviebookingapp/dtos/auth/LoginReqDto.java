package com.example.moviebookingapp.dtos.auth;

import jakarta.validation.constraints.NotBlank;

public record LoginReqDto(
        @NotBlank(message = "Username or email is required") String usernameOrEmail,

        @NotBlank(message = "Password is required") String password) {}
