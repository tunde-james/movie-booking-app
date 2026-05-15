package com.example.moviebookingapp.dtos.movie;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import com.example.moviebookingapp.enums.Genre;
import com.example.moviebookingapp.enums.Language;
import com.example.moviebookingapp.enums.MovieRating;
import com.example.moviebookingapp.enums.MovieStatus;

public record MovieReqDto(

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    String title,

    @Size(max = 2000, message = "Description cannot exceed 1000 characters")
    String description,

    @NotNull(message = "Genre is required")
    Genre genre,

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    Integer durationInMinutes,

    @NotNull(message = "Release date is required")
    @FutureOrPresent(message = "Release date cannot be in the past")
    LocalDate releaseDate,

    @NotNull(message = "Language is required")
    Language language,

    @NotNull(message = "Rating is required")
    MovieRating rating,

    @NotNull(message = "Status is required")
    MovieStatus movieStatus,

    @Size(max = 500, message = "Poster URL cannot exceed 500 characters")
    String posterUrl) {
}
