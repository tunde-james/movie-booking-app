package com.example.moviebookingapp.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.moviebookingapp.dtos.booking.BookingReqDto;
import com.example.moviebookingapp.dtos.booking.BookingResDto;
import com.example.moviebookingapp.entity.Booking;
import com.example.moviebookingapp.entity.Show;
import com.example.moviebookingapp.entity.User;
import com.example.moviebookingapp.enums.BookingStatus;
import com.example.moviebookingapp.enums.ShowStatus;
import com.example.moviebookingapp.exception.BookingNotFoundException;
import com.example.moviebookingapp.exception.InsufficientShowCapacityException;
import com.example.moviebookingapp.exception.InvalidBookingRequestException;
import com.example.moviebookingapp.exception.ShowNotBookableException;
import com.example.moviebookingapp.exception.ShowNotFoundException;
import com.example.moviebookingapp.exception.UserNotFoundException;
import com.example.moviebookingapp.mapper.BookingMapper;
import com.example.moviebookingapp.repository.BookingRepository;
import com.example.moviebookingapp.repository.ShowRepository;
import com.example.moviebookingapp.repository.UserRepository;

@Service
public class BookingService {

    private static final int MAX_TICKET_QUANTITY = 10;

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ShowRepository showRepository;
    private final BookingMapper bookingMapper;

    public BookingService(
            BookingRepository bookingRepository,
            UserRepository userRepository,
            ShowRepository showRepository,
            BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.showRepository = showRepository;
        this.bookingMapper = bookingMapper;
    }

    @Transactional
    public BookingResDto createBooking(BookingReqDto reqDto) {

        BookingReqDto validatedReqDto =
                normalizeBookingRequest(Objects.requireNonNull(reqDto, "Booking request cannot be null"));

        Long userId = validatedReqDto.userId();
        Long showId = Objects.requireNonNull(validatedReqDto.showId(), "Show ID cannot be null");
        Integer ticketQuantity =
                Objects.requireNonNull(validatedReqDto.ticketQuantity(), "Ticket quantity cannot be null");

        if (ticketQuantity <= 0) {
            throw new InvalidBookingRequestException("Ticket quantity must be positive");
        }

        if (ticketQuantity > MAX_TICKET_QUANTITY) {
            throw new InvalidBookingRequestException("Cannot book more than 10 seats at once");
        }

        User user = userId == null
                ? null
                : userRepository
                        .findById(userId)
                        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        Show show = showRepository
                .findById(showId)
                .orElseThrow(() -> new ShowNotFoundException("Show not found with ID: " + showId));

        ensureShowCanAcceptBooking(show, ticketQuantity);

        BigDecimal unitPrice = show.getPricePerTicket();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(ticketQuantity.longValue()));

        Booking booking = Objects.requireNonNull(
                bookingMapper.toEntity(validatedReqDto, user, show, BookingStatus.PENDING, unitPrice, totalPrice),
                "Booking mapper must not return null");

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toDto(savedBooking);
    }

    @Transactional
    public BookingResDto confirmBooking(Long bookingId) {

        Long validatedBookingId = Objects.requireNonNull(bookingId, "Booking ID cannot be null");

        Booking booking = bookingRepository
                .findById(validatedBookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + validatedBookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingRequestException("Only pending bookings can be confirmed");
        }

        Show show = booking.getShow();

        ensureShowCanAcceptBooking(show, booking.getTicketQuantity());

        show.setAvailableCapacity(show.getAvailableCapacity() - booking.getTicketQuantity());
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toDto(savedBooking);
    }

    @Transactional
    public BookingResDto cancelBooking(Long bookingId) {

        Long validatedBookingId = Objects.requireNonNull(bookingId, "Booking ID cannot be null");

        Booking booking = bookingRepository
                .findById(validatedBookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + validatedBookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingRequestException("Booking is already cancelled");
        }

        Show show = booking.getShow();

        if (!show.getStartTime().isAfter(OffsetDateTime.now())) {
            throw new InvalidBookingRequestException("Cannot cancel booking after show has started");
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            show.setAvailableCapacity(show.getAvailableCapacity() + booking.getTicketQuantity());
        }

        booking.setStatus(BookingStatus.CANCELLED);

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toDto(savedBooking);
    }

    private BookingReqDto normalizeBookingRequest(BookingReqDto reqDto) {

        String firstName = requireText(reqDto.firstName(), "First name is required");
        String lastName = requireText(reqDto.lastName(), "Last name is required");
        String email = requireText(reqDto.email(), "Email is required");
        String phoneNumber =
                reqDto.phoneNumber() == null ? null : reqDto.phoneNumber().trim();

        return new BookingReqDto(
                reqDto.userId(),
                firstName,
                lastName,
                email,
                phoneNumber == null || phoneNumber.isBlank() ? null : phoneNumber,
                reqDto.showId(),
                reqDto.ticketQuantity());
    }

    private String requireText(String value, String message) {

        if (value == null || value.isBlank()) {
            throw new InvalidBookingRequestException(message);
        }

        return value.trim();
    }

    private void ensureShowCanAcceptBooking(Show show, Integer ticketQuantity) {

        Integer seatsRequested = Objects.requireNonNull(ticketQuantity, "Ticket quantity cannot be null");

        if (show.getStatus() == ShowStatus.CANCELLED) {
            throw new ShowNotBookableException("This show has been cancelled");
        }

        if (show.getStatus() == ShowStatus.COMPLETED) {
            throw new ShowNotBookableException("This show has already ended");
        }

        if (show.getStatus() != ShowStatus.SCHEDULED) {
            throw new ShowNotBookableException("This show is not available for booking");
        }

        if (!show.getStartTime().isAfter(OffsetDateTime.now())) {
            throw new ShowNotBookableException("This show has already started");
        }

        if (show.getAvailableCapacity() <= 0) {
            throw new ShowNotBookableException("This show is sold out");
        }

        if (seatsRequested > show.getAvailableCapacity()) {
            throw new InsufficientShowCapacityException("Not enough seats available for this show");
        }
    }
}
