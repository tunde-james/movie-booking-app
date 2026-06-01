package com.example.moviebookingapp.controller;

import java.net.URI;
import java.util.Objects;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moviebookingapp.dtos.booking.BookingReqDto;
import com.example.moviebookingapp.dtos.booking.BookingResDto;
import com.example.moviebookingapp.service.BookingService;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResDto> createBooking(@Valid @RequestBody BookingReqDto reqDto) {

        BookingResDto booking = bookingService.createBooking(reqDto);

        URI location = Objects.requireNonNull(URI.create("/api/v1/bookings/" + booking.id()), "");

        return ResponseEntity.created(location).body(booking);
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResDto> confirmBooking(@PathVariable Long bookingId) {

        BookingResDto booking = bookingService.confirmBooking(bookingId);

        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResDto> cancelBooking(@PathVariable Long bookingId) {

        BookingResDto booking = bookingService.cancelBooking(bookingId);

        return ResponseEntity.ok(booking);
    }
}
