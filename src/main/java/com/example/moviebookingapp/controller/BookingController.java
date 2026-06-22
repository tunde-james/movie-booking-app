package com.example.moviebookingapp.controller;

import java.net.URI;
import java.util.Objects;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.moviebookingapp.dtos.booking.BookingReqDto;
import com.example.moviebookingapp.dtos.booking.BookingResDto;
import com.example.moviebookingapp.security.BookingAccessContext;
import com.example.moviebookingapp.service.BookingService;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResDto> getBookingById(
            @PathVariable Long bookingId,
            @RequestHeader(value = "X-Guest-Booking-Token", required = false) String guestAccessToken,
            @AuthenticationPrincipal Jwt jwt) {

        BookingResDto booking = bookingService.getBookingById(bookingId, guestAccessToken, accessContext(jwt));

        return ResponseEntity.ok(booking);
    }

    @PostMapping
    public ResponseEntity<BookingResDto> createBooking(
            @Valid @RequestBody BookingReqDto reqDto, @AuthenticationPrincipal Jwt jwt) {

        BookingResDto booking = bookingService.createBooking(reqDto, accessContext(jwt));

        URI location = Objects.requireNonNull(URI.create("/api/v1/bookings/" + booking.id()), "");

        return ResponseEntity.created(location).body(booking);
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<BookingResDto> confirmBooking(
            @PathVariable Long bookingId,
            @RequestHeader(value = "X-Guest-Booking-Token", required = false) String guestAccessToken,
            @AuthenticationPrincipal Jwt jwt) {

        BookingResDto booking = bookingService.confirmBooking(bookingId, guestAccessToken, accessContext(jwt));

        return ResponseEntity.ok(booking);
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<BookingResDto> cancelBooking(
            @PathVariable Long bookingId,
            @RequestHeader(value = "X-Guest-Booking-Token", required = false) String guestAccessToken,
            @AuthenticationPrincipal Jwt jwt) {

        BookingResDto booking = bookingService.cancelBooking(bookingId, guestAccessToken, accessContext(jwt));

        return ResponseEntity.ok(booking);
    }

    private BookingAccessContext accessContext(Jwt jwt) {

        if (jwt == null) {
            return BookingAccessContext.guest();
        }

        Object userIdClaim = jwt.getClaim("userId");
        Long userId = userIdClaim instanceof Number number ? number.longValue() : null;
        boolean admin = "ADMIN".equals(jwt.getClaimAsString("role"));

        return new BookingAccessContext(userId, admin);
    }
}
