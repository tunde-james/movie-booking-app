package com.example.moviebookingapp.dtos.auditorium;

import com.example.moviebookingapp.enums.AuditoriumType;

public record AuditoriumResDto(Long id, Long cinemaId, String name, AuditoriumType type, Integer capacity) {}
