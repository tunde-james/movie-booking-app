package com.example.moviebookingapp.dtos.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.moviebookingapp.dtos.show.ShowResDto;
import com.example.moviebookingapp.dtos.user.UserResDto;
import com.example.moviebookingapp.enums.BookingStatus;

public record BookingResDto(

    Long id, UserResDto user, ShowResDto show, Integer numberOfSeats, BigDecimal totalPrice,
    BookingStatus bookingStatus, LocalDateTime bookingTime) {

}
