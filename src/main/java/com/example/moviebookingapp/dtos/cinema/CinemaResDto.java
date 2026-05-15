package com.example.moviebookingapp.dtos.cinema;

public record CinemaResDto(

    Long id, String name, String address, Integer capacity, String screenType,
    Integer totalScreens) {

}
