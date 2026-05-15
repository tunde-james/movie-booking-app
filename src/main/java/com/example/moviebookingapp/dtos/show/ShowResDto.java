package com.example.moviebookingapp.dtos.show;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.moviebookingapp.dtos.cinema.CinemaResDto;
import com.example.moviebookingapp.dtos.movie.MovieResDto;
import com.example.moviebookingapp.enums.ShowStatus;

public record ShowResDto(

    Long id, MovieResDto movie, CinemaResDto cinema, LocalDateTime showTime, ShowStatus showStatus,
    Integer totalSeats, Integer availableSeats, BigDecimal price) {

}
