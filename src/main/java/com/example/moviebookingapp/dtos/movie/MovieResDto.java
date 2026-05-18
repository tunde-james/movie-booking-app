package com.example.moviebookingapp.dtos.movie;

import java.time.LocalDate;

import com.example.moviebookingapp.enums.Genre;
import com.example.moviebookingapp.enums.Language;
import com.example.moviebookingapp.enums.MovieRating;
import com.example.moviebookingapp.enums.MovieStatus;

public record MovieResDto(
        Long id,
        String title,
        String description,
        Genre genre,
        Integer durationInMinutes,
        LocalDate releaseDate,
        Language language,
        MovieRating rating,
        MovieStatus movieStatus,
        String posterUrl) {}
