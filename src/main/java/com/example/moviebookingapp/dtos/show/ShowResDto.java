package com.example.moviebookingapp.dtos.show;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.example.moviebookingapp.enums.ShowStatus;

public record ShowResDto(
        Long id,
        Long movieId,
        String movieTitle,
        Long cinemaId,
        String cinemaName,
        Long auditoriumId,
        String auditoriumName,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        Integer totalCapacity,
        Integer availableCapacity,
        BigDecimal pricePerTicket,
        ShowStatus status) {}
