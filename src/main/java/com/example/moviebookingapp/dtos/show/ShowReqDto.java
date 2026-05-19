package com.example.moviebookingapp.dtos.show;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import com.example.moviebookingapp.enums.ShowStatus;

public record ShowReqDto(
        @NotNull(message = "Movie ID is required") Long movieId,

        @NotNull(message = "Cinema ID is required") Long cinemaId,

        @NotNull(message = "Show time is required") @Future(message = "Show time must be in the future")
        LocalDateTime showTime,

        @NotNull(message = "Show status is required") ShowStatus status,

        @NotNull(message = "Total seats is required")
        @Positive(message = "Total seats must be positive")
        @Min(value = 1, message = "Must have at least 1 seat")
        @Max(value = 1000, message = "Cannot exceed 1000 seats")
        Integer totalSeats,

        @NotNull(message = "Price is required")
        @Positive(message = "Price must be positive")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price) {}
