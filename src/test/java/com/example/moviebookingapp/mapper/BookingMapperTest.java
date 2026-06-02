package com.example.moviebookingapp.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

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
    void toEntityCreatesBookingSnapshotFromRequestUserShowAndPrices() {
        
        BookingReqDto reqDto = new BookingReqDto(5L, "Tunde", "James", "tunde@example.com", "08012345678", 10L, 3);

        User user = new User();
        Show show = new Show();
        BigDecimal unitPrice = new BigDecimal("3500.00");
        BigDecimal totalPrice = new BigDecimal("10500.00");

        Booking booking = bookingMapper.toEntity(reqDto, user, show, BookingStatus.PENDING, unitPrice, totalPrice);

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
    }
}
