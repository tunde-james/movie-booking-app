package com.example.moviebookingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.moviebookingapp.dtos.booking.BookingReqDto;
import com.example.moviebookingapp.dtos.booking.BookingResDto;
import com.example.moviebookingapp.entity.BaseEntity;
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

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final String GUEST_TOKEN = "guest-token";

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBookingCreatesPendingBookingWithoutReducingShowCapacity() {

        BookingReqDto request = guestBookingRequest(10L, 2);

        Show show = new Show();
        show.setStatus(ShowStatus.SCHEDULED);
        show.setStartTime(OffsetDateTime.now().plusDays(1));
        show.setAvailableCapacity(50);
        show.setPricePerTicket(new BigDecimal("3500.00"));

        Booking bookingToSave = new Booking();
        Booking savedBooking = new Booking();

        BookingResDto response = new BookingResDto(
                100L,
                null,
                "Ada",
                "Lovelace",
                "ada@example.com",
                null,
                null,
                2,
                new BigDecimal("3500.00"),
                new BigDecimal("7000.00"),
                BookingStatus.PENDING,
                null,
                GUEST_TOKEN);

        when(showRepository.findById(10L)).thenReturn(Optional.of(show));
        when(bookingMapper.toEntity(
                        eq(request),
                        eq(null),
                        eq(show),
                        eq(BookingStatus.PENDING),
                        eq(new BigDecimal("3500.00")),
                        eq(new BigDecimal("7000.00")),
                        any(OffsetDateTime.class),
                        any(String.class)))
                .thenReturn(bookingToSave);
        when(bookingRepository.save(bookingToSave)).thenReturn(savedBooking);
        when(bookingMapper.toDto(savedBooking)).thenReturn(response);

        BookingResDto result = bookingService.createBooking(request);

        assertThat(result).isEqualTo(response);
        assertThat(show.getAvailableCapacity()).isEqualTo(50);

        verify(bookingMapper)
                .toEntity(
                        eq(request),
                        eq(null),
                        eq(show),
                        eq(BookingStatus.PENDING),
                        eq(new BigDecimal("3500.00")),
                        eq(new BigDecimal("7000.00")),
                        any(OffsetDateTime.class),
                        any(String.class));
        verify(userRepository, never()).findById(any(Long.class));
        verify(bookingRepository).save(bookingToSave);
    }

    @Test
    void createBookingReturnsNotFoundWhenUserDoesNotExist() {

        BookingReqDto request = accountBookingRequest(99L, 10L, 2);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with ID: 99");

        verify(showRepository, never()).findById(any(Long.class));
        verify(bookingMapper, never())
                .toEntity(
                        any(BookingReqDto.class),
                        any(User.class),
                        any(Show.class),
                        any(BookingStatus.class),
                        any(BigDecimal.class),
                        any(BigDecimal.class),
                        any(OffsetDateTime.class),
                        any(String.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBookingReturnsNotFoundWhenShowDoesNotExist() {

        BookingReqDto request = guestBookingRequest(99L, 2);

        when(showRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(ShowNotFoundException.class)
                .hasMessage("Show not found with ID: 99");

        verify(bookingMapper, never())
                .toEntity(
                        any(BookingReqDto.class),
                        any(User.class),
                        any(Show.class),
                        any(BookingStatus.class),
                        any(BigDecimal.class),
                        any(BigDecimal.class),
                        any(OffsetDateTime.class),
                        any(String.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBookingRejectsCancelledShow() {

        BookingReqDto request = guestBookingRequest(10L, 2);

        Show show = new Show();
        show.setStatus(ShowStatus.CANCELLED);
        show.setStartTime(OffsetDateTime.now().plusDays(1));
        show.setAvailableCapacity(50);
        show.setPricePerTicket(new BigDecimal("3500.00"));

        when(showRepository.findById(10L)).thenReturn(Optional.of(show));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(ShowNotBookableException.class)
                .hasMessage("This show has been cancelled");

        verify(bookingMapper, never())
                .toEntity(
                        any(BookingReqDto.class),
                        any(User.class),
                        any(Show.class),
                        any(BookingStatus.class),
                        any(BigDecimal.class),
                        any(BigDecimal.class),
                        any(OffsetDateTime.class),
                        any(String.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBookingRejectsPastShow() {

        BookingReqDto request = guestBookingRequest(10L, 2);

        Show show = new Show();
        show.setStatus(ShowStatus.SCHEDULED);
        show.setStartTime(OffsetDateTime.now().minusMinutes(1));
        show.setAvailableCapacity(50);
        show.setPricePerTicket(new BigDecimal("3500.00"));

        when(showRepository.findById(10L)).thenReturn(Optional.of(show));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(ShowNotBookableException.class)
                .hasMessage("This show has already started");

        verify(bookingMapper, never())
                .toEntity(
                        any(BookingReqDto.class),
                        any(User.class),
                        any(Show.class),
                        any(BookingStatus.class),
                        any(BigDecimal.class),
                        any(BigDecimal.class),
                        any(OffsetDateTime.class),
                        any(String.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBookingRejectsSoldOutShow() {

        BookingReqDto request = guestBookingRequest(10L, 2);

        Show show = new Show();
        show.setStatus(ShowStatus.SCHEDULED);
        show.setStartTime(OffsetDateTime.now().plusDays(1));
        show.setAvailableCapacity(0);
        show.setPricePerTicket(new BigDecimal("3500.00"));

        when(showRepository.findById(10L)).thenReturn(Optional.of(show));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(ShowNotBookableException.class)
                .hasMessage("This show is sold out");

        verify(bookingMapper, never())
                .toEntity(
                        any(BookingReqDto.class),
                        any(User.class),
                        any(Show.class),
                        any(BookingStatus.class),
                        any(BigDecimal.class),
                        any(BigDecimal.class),
                        any(OffsetDateTime.class),
                        any(String.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBookingRejectsQuantityGreaterThanAvailableCapacity() {

        BookingReqDto request = guestBookingRequest(10L, 2);

        Show show = new Show();
        show.setStatus(ShowStatus.SCHEDULED);
        show.setStartTime(OffsetDateTime.now().plusDays(1));
        show.setAvailableCapacity(1);
        show.setPricePerTicket(new BigDecimal("3500.00"));

        when(showRepository.findById(10L)).thenReturn(Optional.of(show));

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(InsufficientShowCapacityException.class)
                .hasMessage("Not enough seats available for this show");

        verify(bookingMapper, never())
                .toEntity(
                        any(BookingReqDto.class),
                        any(User.class),
                        any(Show.class),
                        any(BookingStatus.class),
                        any(BigDecimal.class),
                        any(BigDecimal.class),
                        any(OffsetDateTime.class),
                        any(String.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBookingRejectsNonPositiveTicketQuantity() {

        BookingReqDto request = guestBookingRequest(10L, 0);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(InvalidBookingRequestException.class)
                .hasMessage("Ticket quantity must be positive");

        verify(userRepository, never()).findById(any(Long.class));
        verify(showRepository, never()).findById(any(Long.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBookingRejectsTicketQuantityAboveLimit() {

        BookingReqDto request = guestBookingRequest(10L, 11);

        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(InvalidBookingRequestException.class)
                .hasMessage("Cannot book more than 10 seats at once");

        verify(userRepository, never()).findById(any(Long.class));
        verify(showRepository, never()).findById(any(Long.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void confirmBookingConfirmsPendingBookingAndReducesShowCapacity() {

        Long bookingId = 100L;

        Show show = new Show();
        setId(show, 10L);
        show.setStatus(ShowStatus.SCHEDULED);
        show.setStartTime(OffsetDateTime.now().plusDays(1));
        show.setAvailableCapacity(50);

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.PENDING);
        booking.setShow(show);
        booking.setTicketQuantity(2);
        booking.setGuestAccessToken(GUEST_TOKEN);

        Booking savedBooking = new Booking();

        BookingResDto response = new BookingResDto(
                bookingId,
                null,
                "Ada",
                "Lovelace",
                "ada@example.com",
                null,
                null,
                2,
                new BigDecimal("3500.00"),
                new BigDecimal("7000.00"),
                BookingStatus.CONFIRMED,
                null,
                GUEST_TOKEN);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(showRepository.findByIdWithPessimisticWriteLock(10L)).thenReturn(Optional.of(show));
        when(bookingRepository.save(booking)).thenReturn(savedBooking);
        when(bookingMapper.toDto(savedBooking)).thenReturn(response);

        BookingResDto result = bookingService.confirmBooking(bookingId, GUEST_TOKEN);

        assertThat(result).isEqualTo(response);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(show.getAvailableCapacity()).isEqualTo(48);

        verify(bookingRepository).save(booking);
    }

    @Test
    void confirmBookingReturnsNotFoundWhenBookingDoesNotExist() {

        Long bookingId = 999L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.confirmBooking(bookingId, GUEST_TOKEN))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("Booking not found with ID: 999");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void confirmBookingRejectsAlreadyConfirmedBooking() {

        Long bookingId = 100L;

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setGuestAccessToken(GUEST_TOKEN);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmBooking(bookingId, GUEST_TOKEN))
                .isInstanceOf(InvalidBookingRequestException.class)
                .hasMessage("Only pending bookings can be confirmed");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void confirmBookingRejectsCancelledBooking() {

        Long bookingId = 100L;

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setGuestAccessToken(GUEST_TOKEN);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmBooking(bookingId, GUEST_TOKEN))
                .isInstanceOf(InvalidBookingRequestException.class)
                .hasMessage("Only pending bookings can be confirmed");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void confirmBookingRejectsWhenShowNoLongerHasEnoughCapacity() {

        Long bookingId = 100L;

        Show show = new Show();
        setId(show, 10L);
        show.setStatus(ShowStatus.SCHEDULED);
        show.setStartTime(OffsetDateTime.now().plusDays(1));
        show.setAvailableCapacity(1);

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.PENDING);
        booking.setShow(show);
        booking.setTicketQuantity(2);
        booking.setGuestAccessToken(GUEST_TOKEN);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(showRepository.findByIdWithPessimisticWriteLock(10L)).thenReturn(Optional.of(show));

        assertThatThrownBy(() -> bookingService.confirmBooking(bookingId, GUEST_TOKEN))
                .isInstanceOf(InsufficientShowCapacityException.class)
                .hasMessage("Not enough seats available for this show");

        assertThat(show.getAvailableCapacity()).isEqualTo(1);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void confirmBookingRejectsWhenShowHasAlreadyStarted() {

        Long bookingId = 100L;

        Show show = new Show();
        setId(show, 10L);
        show.setStatus(ShowStatus.SCHEDULED);
        show.setStartTime(OffsetDateTime.now().minusMinutes(1));
        show.setAvailableCapacity(50);

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.PENDING);
        booking.setShow(show);
        booking.setTicketQuantity(2);
        booking.setGuestAccessToken(GUEST_TOKEN);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(showRepository.findByIdWithPessimisticWriteLock(10L)).thenReturn(Optional.of(show));

        assertThatThrownBy(() -> bookingService.confirmBooking(bookingId, GUEST_TOKEN))
                .isInstanceOf(ShowNotBookableException.class)
                .hasMessage("This show has already started");

        assertThat(show.getAvailableCapacity()).isEqualTo(50);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBookingCancelsPendingBookingWithoutChangingShowCapacity() {

        Long bookingId = 100L;

        Show show = new Show();
        show.setStartTime(OffsetDateTime.now().plusDays(1));
        show.setAvailableCapacity(50);

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.PENDING);
        booking.setShow(show);
        booking.setTicketQuantity(2);
        booking.setGuestAccessToken(GUEST_TOKEN);

        Booking savedBooking = new Booking();

        BookingResDto response = new BookingResDto(
                bookingId,
                null,
                "Ada",
                "Lovelace",
                "ada@example.com",
                null,
                null,
                2,
                new BigDecimal("3500.00"),
                new BigDecimal("7000.00"),
                BookingStatus.CANCELLED,
                null,
                GUEST_TOKEN);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(savedBooking);
        when(bookingMapper.toDto(savedBooking)).thenReturn(response);

        BookingResDto result = bookingService.cancelBooking(bookingId, GUEST_TOKEN);

        assertThat(result).isEqualTo(response);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(show.getAvailableCapacity()).isEqualTo(50);

        verify(bookingRepository).save(booking);
    }

    @Test
    void cancelBookingCancelsConfirmedBookingAndRestoresShowCapacity() {

        Long bookingId = 100L;

        Show show = new Show();
        setId(show, 10L);
        show.setStartTime(OffsetDateTime.now().plusDays(1));
        show.setAvailableCapacity(48);

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setShow(show);
        booking.setTicketQuantity(2);
        booking.setGuestAccessToken(GUEST_TOKEN);

        Booking savedBooking = new Booking();

        BookingResDto response = new BookingResDto(
                bookingId,
                null,
                "Ada",
                "Lovelace",
                "ada@example.com",
                null,
                null,
                2,
                new BigDecimal("3500.00"),
                new BigDecimal("7000.00"),
                BookingStatus.CANCELLED,
                null,
                GUEST_TOKEN);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(showRepository.findByIdWithPessimisticWriteLock(10L)).thenReturn(Optional.of(show));
        when(bookingRepository.save(booking)).thenReturn(savedBooking);
        when(bookingMapper.toDto(savedBooking)).thenReturn(response);

        BookingResDto result = bookingService.cancelBooking(bookingId, GUEST_TOKEN);

        assertThat(result).isEqualTo(response);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(show.getAvailableCapacity()).isEqualTo(50);

        verify(bookingRepository).save(booking);
    }

    @Test
    void cancelBookingReturnsNotFoundWhenBookingDoesNotExist() {

        Long bookingId = 999L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, GUEST_TOKEN))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("Booking not found with ID: 999");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBookingRejectsAlreadyCancelledBooking() {

        Long bookingId = 100L;

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setGuestAccessToken(GUEST_TOKEN);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, GUEST_TOKEN))
                .isInstanceOf(InvalidBookingRequestException.class)
                .hasMessage("Booking is already cancelled");

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void cancelBookingRejectsWhenShowHasAlreadyStarted() {

        Long bookingId = 100L;

        Show show = new Show();
        show.setStartTime(OffsetDateTime.now().minusMinutes(1));
        show.setAvailableCapacity(48);

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setShow(show);
        booking.setTicketQuantity(2);
        booking.setGuestAccessToken(GUEST_TOKEN);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.cancelBooking(bookingId, GUEST_TOKEN))
                .isInstanceOf(InvalidBookingRequestException.class)
                .hasMessage("Cannot cancel booking after show has started");

        assertThat(show.getAvailableCapacity()).isEqualTo(48);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getBookingByIdReturnsBookingWhenItExists() {

        Long bookingId = 100L;

        Booking booking = new Booking();
        booking.setGuestAccessToken(GUEST_TOKEN);

        BookingResDto response = new BookingResDto(
                bookingId,
                null,
                "Ada",
                "Lovelace",
                "ada@example.com",
                null,
                null,
                2,
                new BigDecimal("3500.00"),
                new BigDecimal("7000.00"),
                BookingStatus.CONFIRMED,
                null,
                GUEST_TOKEN);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(response);

        BookingResDto result = bookingService.getBookingById(bookingId, GUEST_TOKEN);

        assertThat(result).isEqualTo(response);

        verify(bookingMapper).toDto(booking);
    }

    @Test
    void getBookingByIdRejectsInvalidGuestToken() {

        Long bookingId = 100L;

        Booking booking = new Booking();
        booking.setGuestAccessToken(GUEST_TOKEN);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingById(bookingId, "wrong-token"))
                .isInstanceOf(InvalidBookingRequestException.class)
                .hasMessage("Guest booking token is invalid");

        verify(bookingMapper, never()).toDto(any(Booking.class));
    }

    @Test
    void getBookingByIdReturnsNotFoundWhenBookingDoesNotExist() {
        Long bookingId = 999L;

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(bookingId, GUEST_TOKEN))
                .isInstanceOf(BookingNotFoundException.class)
                .hasMessage("Booking not found with ID: 999");

        verify(bookingMapper, never()).toDto(any(Booking.class));
    }

    @Test
    void confirmBookingRejectsInvalidGuestToken() {

        Long bookingId = 100L;

        Booking booking = new Booking();
        booking.setStatus(BookingStatus.PENDING);
        booking.setGuestAccessToken(GUEST_TOKEN);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.confirmBooking(bookingId, "wrong-token"))
                .isInstanceOf(InvalidBookingRequestException.class)
                .hasMessage("Guest booking token is invalid");

        verify(showRepository, never()).findByIdWithPessimisticWriteLock(any(Long.class));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    private static void setId(BaseEntity entity, Long id) {
        try {
            Field field = BaseEntity.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
    }

    private BookingReqDto guestBookingRequest(Long showId, Integer ticketQuantity) {

        return new BookingReqDto(null, showId, "Ada", "Lovelace", "ada@example.com", null, ticketQuantity);
    }

    private BookingReqDto accountBookingRequest(Long userId, Long showId, Integer ticketQuantity) {

        return new BookingReqDto(userId, showId, "Ada", "Lovelace", "ada@example.com", null, ticketQuantity);
    }
}
