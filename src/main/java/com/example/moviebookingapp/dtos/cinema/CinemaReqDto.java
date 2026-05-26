package com.example.moviebookingapp.dtos.cinema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CinemaReqDto(
        @NotBlank(message = "Cinema name is required")
        @Size(max = 150, message = "Cinema name cannot exceed 150 characters")
        String name,

        @NotBlank(message = "Address is required") @Size(max = 255, message = "Address cannot exceed 255 characters")
        String address,

        @NotBlank(message = "City is required") @Size(max = 100, message = "City cannot exceed 100 characters")
        String city) {}
