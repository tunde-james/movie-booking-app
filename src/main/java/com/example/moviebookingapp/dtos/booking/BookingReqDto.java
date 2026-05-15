package com.example.moviebookingapp.dtos.booking;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BookingReqDto(

    @NotNull(message = "User ID is required")
    Long userId,

    @NotNull(message = "Show ID is required")
    Long showId,

    @NotNull(message = "Number of seats is required")
    @Positive(message = "Number of seats must be positive")
    @Min(value = 1, message = "Must book at least 1 seat")
    @Max(value = 10, message = "Cannot book more than 10 seats at once")
    Integer numberOfSeats) {
}
