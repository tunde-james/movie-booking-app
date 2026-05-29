package com.example.moviebookingapp.dtos.show;

import java.time.LocalDate;

import com.example.moviebookingapp.enums.ShowStatus;

public record ShowSearchCriteria(
        Long movieId,
        String movieTitle,
        Long cinemaId,
        String cinemaName,
        String city,
        Long auditoriumId,
        LocalDate date,
        ShowStatus status) {}
