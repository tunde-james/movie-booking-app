package com.example.moviebookingapp.dtos.cinema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CinemaReqDto(
        @NotBlank(message = "Cinema name is required") String name,

        @NotBlank(message = "Address is required") String address,

        @NotNull(message = "Capacity is required") @Positive(message = "Total capacity must be positive")
        Integer capacity,

        String screenType,

        @NotNull(message = "Total screens is required")
        @Positive(message = "Total screens must be positive")
        @Min(value = 1, message = "Must have at least 1 screen")
        Integer totalScreens) {}
