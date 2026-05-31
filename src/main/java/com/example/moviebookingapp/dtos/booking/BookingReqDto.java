package com.example.moviebookingapp.dtos.booking;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BookingReqDto(
        Long userId,

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name cannot exceed 100 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name cannot exceed 100 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 150, message = "Email cannot exceed 150 characters")
        String email,

        @Size(max = 20, message = "Phone number cannot exceed 20 characters")
        String phoneNumber,

        @NotNull(message = "Show ID is required") Long showId,

        @NotNull(message = "Number of seats is required")
        @Positive(message = "Number of seats must be positive")
        @Min(value = 1, message = "Must book at least 1 seat")
        @Max(value = 10, message = "Cannot book more than 10 seats at once")
        Integer ticketQuantity) {}
