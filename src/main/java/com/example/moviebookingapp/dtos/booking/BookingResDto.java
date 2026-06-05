package com.example.moviebookingapp.dtos.booking;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import com.example.moviebookingapp.dtos.show.ShowResDto;
import com.example.moviebookingapp.dtos.user.UserResDto;
import com.example.moviebookingapp.enums.BookingStatus;

public record BookingResDto(
        Long id,
        UserResDto user,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        ShowResDto show,
        Integer ticketQuantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        BookingStatus status,
        OffsetDateTime bookingTime,
        String guestAccessToken) {}
