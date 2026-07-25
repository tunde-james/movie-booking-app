package com.example.moviebookingapp.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordReqDto(
        @NotBlank(message = "Current password is required") String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters")
        @Pattern(regexp = ".*[A-Z].*", message = "New password must contain at least one uppercase letter")
        @Pattern(regexp = ".*[a-z].*", message = "New password must contain at least one lowercase letter")
        @Pattern(regexp = ".*\\d.*", message = "New password must contain at least one number")
        String newPassword) {}
