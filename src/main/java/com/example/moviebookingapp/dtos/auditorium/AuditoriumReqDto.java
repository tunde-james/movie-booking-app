package com.example.moviebookingapp.dtos.auditorium;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import com.example.moviebookingapp.enums.AuditoriumType;

public record AuditoriumReqDto(
        @NotBlank(message = "Auditorium name is required")
        @Size(max = 100, message = "Auditorium name cannot exceed 100 characters")
        String name,

        @NotNull(message = "Auditorium type is required") AuditoriumType type,

        @NotNull(message = "Capacity is required") @Positive(message = "Capacity must be positive")
        Integer capacity) {}
