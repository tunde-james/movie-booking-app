package com.example.moviebookingapp.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.moviebookingapp.dtos.booking.BookingReqDto;
import com.example.moviebookingapp.dtos.booking.BookingResDto;
import com.example.moviebookingapp.entity.Booking;
import com.example.moviebookingapp.entity.Show;
import com.example.moviebookingapp.entity.User;
import com.example.moviebookingapp.enums.BookingStatus;

@Mapper(
        config = BaseMapperConfig.class,
        uses = {UserMapper.class, ShowMapper.class})
public interface BookingMapper {

    @Mapping(target = "user", source = "user")
    @Mapping(target = "firstName", source = "reqDto.firstName")
    @Mapping(target = "lastName", source = "reqDto.lastName")
    @Mapping(target = "email", source = "reqDto.email")
    @Mapping(target = "phoneNumber", source = "reqDto.phoneNumber")
    @Mapping(target = "show", source = "show")
    @Mapping(target = "ticketQuantity", source = "reqDto.ticketQuantity")
    @Mapping(target = "unitPrice", source = "unitPrice")
    @Mapping(target = "totalPrice", source = "totalPrice")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "bookingTime", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "deleted", ignore = true)
    Booking toEntity(
            BookingReqDto reqDto,
            User user,
            Show show,
            BookingStatus status,
            BigDecimal unitPrice,
            BigDecimal totalPrice);

    BookingResDto toDto(Booking booking);

    List<BookingResDto> toDtoList(List<Booking> bookings);
}
