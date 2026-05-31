package com.example.moviebookingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.example.moviebookingapp.entity.Booking;
import com.example.moviebookingapp.entity.Show;
import com.example.moviebookingapp.entity.User;
import com.example.moviebookingapp.enums.BookingStatus;
import com.example.moviebookingapp.enums.ShowStatus;
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
                null);

        when(showRepository.findById(10L)).thenReturn(Optional.of(show));
        when(bookingMapper.toEntity(
                        request,
                        null,
                        show,
                        BookingStatus.PENDING,
                        new BigDecimal("3500.00"),
                        new BigDecimal("7000.00")))
                .thenReturn(bookingToSave);
        when(bookingRepository.save(bookingToSave)).thenReturn(savedBooking);
        when(bookingMapper.toDto(savedBooking)).thenReturn(response);

        BookingResDto result = bookingService.createBooking(request);

        assertThat(result).isEqualTo(response);
        assertThat(show.getAvailableCapacity()).isEqualTo(50);

        verify(bookingMapper)
                .toEntity(
                        request,
                        null,
                        show,
                        BookingStatus.PENDING,
                        new BigDecimal("3500.00"),
                        new BigDecimal("7000.00"));
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
                        any(BigDecimal.class));
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
                        any(BigDecimal.class));
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
                        any(BigDecimal.class));
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
                        any(BigDecimal.class));
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
                        any(BigDecimal.class));
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
                        any(BigDecimal.class));
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

    private BookingReqDto guestBookingRequest(Long showId, Integer ticketQuantity) {

        return new BookingReqDto(null, "Ada", "Lovelace", "ada@example.com", null, showId, ticketQuantity);
    }

    private BookingReqDto accountBookingRequest(Long userId, Long showId, Integer ticketQuantity) {

        return new BookingReqDto(userId, "Ada", "Lovelace", "ada@example.com", null, showId, ticketQuantity);
    }
}
