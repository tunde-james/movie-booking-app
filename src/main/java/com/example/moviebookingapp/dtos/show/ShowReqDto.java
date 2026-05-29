package com.example.moviebookingapp.dtos.show;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ShowReqDto(
        @NotNull(message = "Movie ID is required") Long movieId,

        @NotNull(message = "Auditorium ID is required") Long auditoriumId,

        @NotNull(message = "Start time is required") @Future(message = "Start time must be in the future")
        OffsetDateTime startTime,

        @NotNull(message = "End time is required") OffsetDateTime endTime,

        @NotNull(message = "Price per ticket is required")
        @Positive(message = "Price per ticket must be positive")
        @DecimalMin(value = "0.01", message = "Price per ticket must be greater than 0")
        BigDecimal pricePerTicket) {}
