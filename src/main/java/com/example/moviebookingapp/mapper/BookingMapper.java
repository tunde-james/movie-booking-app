package com.example.moviebookingapp.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.moviebookingapp.dtos.booking.BookingReqDto;
import com.example.moviebookingapp.dtos.booking.BookingResDto;
import com.example.moviebookingapp.entity.Booking;

@Mapper(
        config = BaseMapperConfig.class,
        uses = {UserMapper.class, ShowMapper.class})
public interface BookingMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "show", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "bookingStatus", ignore = true)
    @Mapping(target = "bookingTime", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Booking toEntity(BookingReqDto req);

    BookingResDto toDto(Booking booking);

    List<BookingResDto> toDtoList(List<Booking> bookings);
}
