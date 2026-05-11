package com.example.moviebookingapp.dtos.movie;

import java.time.LocalDate;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.example.moviebookingapp.enums.Genre;
import com.example.moviebookingapp.enums.Language;
import com.example.moviebookingapp.enums.MovieRating;
import com.example.moviebookingapp.enums.MovieStatus;

@Getter
@Setter
@NoArgsConstructor
public class MovieReqDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Genre is required")
    private Genre genre;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationInMinutes;

    @NotNull(message = "Release date is required")
    @FutureOrPresent(message = "Release date cannot be in the past")
    private LocalDate releaseDate;

    @NotNull(message = "Language is required")
    private Language language;

    @NotNull(message = "Rating is required")
    private MovieRating rating;

    @NotNull(message = "Status is required")
    private MovieStatus status;

    private String posterUrl;
}
