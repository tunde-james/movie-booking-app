package com.example.moviebookingapp.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import com.example.moviebookingapp.dtos.booking.BookingReqDto;
import com.example.moviebookingapp.entity.Booking;
import com.example.moviebookingapp.entity.Show;
import com.example.moviebookingapp.entity.User;
import com.example.moviebookingapp.enums.BookingStatus;

class BookingMapperTest {

    private final BookingMapper bookingMapper = Mappers.getMapper(BookingMapper.class);

    @Test
    void toEntityCreatesGuestBookingSnapshotWhenUserIsNull() {

        BookingReqDto reqDto = new BookingReqDto(null, 10L, "Ada", "Lovelace", "ada@example.com", "08098765432", 2);

        Show show = new Show();
        BigDecimal unitPrice = new BigDecimal("3500.00");
        BigDecimal totalPrice = new BigDecimal("7000.00");
        OffsetDateTime bookingTime = OffsetDateTime.now();

        Booking booking = bookingMapper.toEntity(
                reqDto, null, show, BookingStatus.PENDING, unitPrice, totalPrice, bookingTime, "guest-token");

        assertThat(booking.getUser()).isNull();
        assertThat(booking.getShow()).isSameAs(show);
        assertThat(booking.getFirstName()).isEqualTo("Ada");
        assertThat(booking.getLastName()).isEqualTo("Lovelace");
        assertThat(booking.getEmail()).isEqualTo("ada@example.com");
        assertThat(booking.getPhoneNumber()).isEqualTo("08098765432");
        assertThat(booking.getTicketQuantity()).isEqualTo(2);
        assertThat(booking.getUnitPrice()).isEqualByComparingTo("3500.00");
        assertThat(booking.getTotalPrice()).isEqualByComparingTo("7000.00");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(booking.getBookingTime()).isEqualTo(bookingTime);
        assertThat(booking.getGuestAccessToken()).isEqualTo("guest-token");
    }

    @Test
    void toEntityCreatesBookingSnapshotFromRequestUserShowAndPrices() {

        BookingReqDto reqDto = new BookingReqDto(5L, 10L, "Tunde", "James", "tunde@example.com", "08012345678", 3);

        User user = new User();
        Show show = new Show();
        BigDecimal unitPrice = new BigDecimal("3500.00");
        BigDecimal totalPrice = new BigDecimal("10500.00");
        OffsetDateTime bookingTime = OffsetDateTime.now();

        Booking booking = bookingMapper.toEntity(
                reqDto, user, show, BookingStatus.PENDING, unitPrice, totalPrice, bookingTime, "guest-token");

        assertThat(booking.getUser()).isSameAs(user);
        assertThat(booking.getShow()).isSameAs(show);
        assertThat(booking.getFirstName()).isEqualTo("Tunde");
        assertThat(booking.getLastName()).isEqualTo("James");
        assertThat(booking.getEmail()).isEqualTo("tunde@example.com");
        assertThat(booking.getPhoneNumber()).isEqualTo("08012345678");
        assertThat(booking.getTicketQuantity()).isEqualTo(3);
        assertThat(booking.getUnitPrice()).isEqualByComparingTo("3500.00");
        assertThat(booking.getTotalPrice()).isEqualByComparingTo("10500.00");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(booking.getBookingTime()).isNotNull();
        assertThat(booking.getGuestAccessToken()).isEqualTo("guest-token");
    }
}
