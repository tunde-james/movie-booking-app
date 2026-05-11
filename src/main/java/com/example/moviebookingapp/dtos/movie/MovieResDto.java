package com.example.moviebookingapp.dtos.movie;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
@AllArgsConstructor
@Builder
public class MovieResDto {

    private Long id;
    private String title;
    private String description;
    private Genre genre;
    private Integer durationInMinutes;
    private LocalDate releaseDate;
    private Language language;
    private MovieRating rating;
    private MovieStatus status;
    private String posterUrl;
}
