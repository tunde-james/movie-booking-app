package com.example.moviebookingapp.service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
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
import com.example.moviebookingapp.security.BookingAccessContext;

@Service
public class BookingService {

    private static final int MAX_TICKET_QUANTITY = 10;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

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

        return createBooking(reqDto, BookingAccessContext.guest());
    }

    @Transactional
    public BookingResDto createBooking(BookingReqDto reqDto, BookingAccessContext accessContext) {

        BookingAccessContext caller = normalizeAccessContext(accessContext);
        BookingReqDto validatedReqDto =
                normalizeBookingRequest(Objects.requireNonNull(reqDto, "Booking request cannot be null"));

        Long userId = caller.authenticated() ? caller.userId() : null;
        BookingReqDto bookingReqDto = withUserId(validatedReqDto, userId);

        Long showId = Objects.requireNonNull(bookingReqDto.showId(), "Show ID cannot be null");
        Integer ticketQuantity =
                Objects.requireNonNull(bookingReqDto.ticketQuantity(), "Ticket quantity cannot be null");

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

        String guestAccessToken = generateGuestAccessToken();

        Booking booking = Objects.requireNonNull(
                bookingMapper.toEntity(
                        bookingReqDto,
                        user,
                        show,
                        BookingStatus.PENDING,
                        unitPrice,
                        totalPrice,
                        OffsetDateTime.now(),
                        guestAccessToken),
                "Booking mapper must not return null");

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toDto(savedBooking);
    }

    @Transactional
    public BookingResDto confirmBooking(Long bookingId, String guessAccessToken) {

        return confirmBooking(bookingId, guessAccessToken, BookingAccessContext.guest());
    }

    @Transactional
    public BookingResDto confirmBooking(Long bookingId, String guestAccessToken, BookingAccessContext accessContext) {

        Long validatedBookingId = Objects.requireNonNull(bookingId, "Booking ID cannot be null");

        Booking booking = bookingRepository
                .findById(validatedBookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + validatedBookingId));

        ensureBookingAccessMatches(booking, guestAccessToken, accessContext);

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new InvalidBookingRequestException("Only pending bookings can be confirmed");
        }

        Show show = showRepository
                .findByIdWithPessimisticWriteLock(booking.getShow().getId())
                .orElseThrow(() -> new ShowNotFoundException("Show no longer exists"));

        ensureShowCanAcceptBooking(show, booking.getTicketQuantity());

        show.setAvailableCapacity(show.getAvailableCapacity() - booking.getTicketQuantity());
        booking.setStatus(BookingStatus.CONFIRMED);

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toDto(savedBooking);
    }

    @Transactional
    public BookingResDto cancelBooking(Long bookingId, String guestAccessToken) {

        return cancelBooking(bookingId, guestAccessToken, BookingAccessContext.guest());
    }

    @Transactional
    public BookingResDto cancelBooking(Long bookingId, String guestAccessToken, BookingAccessContext accessContext) {

        Long validatedBookingId = Objects.requireNonNull(bookingId, "Booking ID cannot be null");

        Booking booking = bookingRepository
                .findById(validatedBookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + validatedBookingId));

        ensureBookingAccessMatches(booking, guestAccessToken, accessContext);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingRequestException("Booking is already cancelled");
        }

        Show show = booking.getShow();

        if (!show.getStartTime().isAfter(OffsetDateTime.now())) {
            throw new InvalidBookingRequestException("Cannot cancel booking after show has started");
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            show = showRepository
                    .findByIdWithPessimisticWriteLock(show.getId())
                    .orElseThrow(() -> new ShowNotFoundException("Show no longer exists"));

            show.setAvailableCapacity(show.getAvailableCapacity() + booking.getTicketQuantity());
        }

        booking.setStatus(BookingStatus.CANCELLED);

        Booking savedBooking = bookingRepository.save(booking);

        return bookingMapper.toDto(savedBooking);
    }

    @Transactional(readOnly = true)
    public BookingResDto getBookingById(Long bookingId, String guestAccessToken) {

        return getBookingById(bookingId, guestAccessToken, BookingAccessContext.guest());
    }

    @Transactional(readOnly = true)
    public BookingResDto getBookingById(Long bookingId, String guestAccessToken, BookingAccessContext accessContext) {

        Long validatedBookingId = Objects.requireNonNull(bookingId, "Booking ID cannot be null");

        Booking booking = bookingRepository
                .findById(validatedBookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + validatedBookingId));

        ensureBookingAccessMatches(booking, guestAccessToken, accessContext);

        return bookingMapper.toDto(booking);
    }

    private BookingAccessContext normalizeAccessContext(BookingAccessContext accessContext) {
        return accessContext == null ? BookingAccessContext.guest() : accessContext;
    }

    private void ensureBookingAccessMatches(
            Booking booking, String guestAccessToken, BookingAccessContext accessContext) {

        BookingAccessContext caller = normalizeAccessContext(accessContext);

        if (caller.admin()) {
            return;
        }

        if (caller.authenticated()) {

            User user = booking.getUser();

            if (user != null && caller.userId().equals(user.getId())) {
                return;
            }

            if (guestTokenMatches(booking, guestAccessToken)) {
                return;
            }

            throw new AccessDeniedException("You do not have access to this booking");
        }

        ensureGuestTokenMatches(booking, guestAccessToken);
    }

    private BookingReqDto withUserId(BookingReqDto reqDto, Long userId) {

        return new BookingReqDto(
                userId,
                reqDto.showId(),
                reqDto.firstName(),
                reqDto.lastName(),
                reqDto.email(),
                reqDto.phoneNumber(),
                reqDto.ticketQuantity());
    }

    private BookingReqDto normalizeBookingRequest(BookingReqDto reqDto) {

        String firstName = requireText(reqDto.firstName(), "First name is required");
        String lastName = requireText(reqDto.lastName(), "Last name is required");
        String email = requireText(reqDto.email(), "Email is required");
        String phoneNumber =
                reqDto.phoneNumber() == null ? null : reqDto.phoneNumber().trim();

        return new BookingReqDto(
                reqDto.userId(),
                reqDto.showId(),
                firstName,
                lastName,
                email,
                phoneNumber == null || phoneNumber.isBlank() ? null : phoneNumber,
                reqDto.ticketQuantity());
    }

    private String requireText(String value, String message) {

        if (value == null || value.isBlank()) {
            throw new InvalidBookingRequestException(message);
        }

        return value.trim();
    }

    private String generateGuestAccessToken() {

        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        return HexFormat.of().formatHex(bytes);
    }

    private void ensureGuestTokenMatches(Booking booking, String guestAccessToken) {

        if (guestAccessToken == null || guestAccessToken.isBlank()) {
            throw new InvalidBookingRequestException("Guest booking token is required");
        }

        if (!guestTokenMatches(booking, guestAccessToken)) {
            throw new InvalidBookingRequestException("Guest booking token is invalid");
        }
    }

    private boolean guestTokenMatches(Booking booking, String guestAccessToken) {

        if (guestAccessToken == null || guestAccessToken.isBlank()) {
            return false;
        }

        String savedGuestAccessToken = booking.getGuestAccessToken();

        return savedGuestAccessToken != null && constantTimeEquals(savedGuestAccessToken, guestAccessToken.trim());
    }

    private boolean constantTimeEquals(String a, String b) {

        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);

        return MessageDigest.isEqual(aBytes, bBytes);
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
