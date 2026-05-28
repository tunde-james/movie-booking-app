package com.example.moviebookingapp.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.example.moviebookingapp.dtos.show.ShowReqDto;
import com.example.moviebookingapp.dtos.show.ShowResDto;
import com.example.moviebookingapp.entity.Auditorium;
import com.example.moviebookingapp.entity.Movie;
import com.example.moviebookingapp.entity.Show;
import com.example.moviebookingapp.enums.ShowStatus;

@Mapper(config = BaseMapperConfig.class)
public interface ShowMapper {

    @Mapping(target = "movie", source = "movie")
    @Mapping(target = "auditorium", source = "auditorium")
    @Mapping(target = "startTime", source = "reqDto.startTime")
    @Mapping(target = "endTime", source = "reqDto.endTime")
    @Mapping(target = "pricePerTicket", source = "reqDto.pricePerTicket")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "totalCapacity", source = "capacity")
    @Mapping(target = "availableCapacity", source = "capacity")
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    Show toEntity(ShowReqDto reqDto, Movie movie, Auditorium auditorium, ShowStatus status, Integer capacity);

    @Mapping(target = "movieId", source = "movie.id")
    @Mapping(target = "movieTitle", source = "movie.title")
    @Mapping(target = "cinemaId", source = "auditorium.cinema.id")
    @Mapping(target = "cinemaName", source = "auditorium.cinema.name")
    @Mapping(target = "auditoriumId", source = "auditorium.id")
    @Mapping(target = "auditoriumName", source = "auditorium.name")
    ShowResDto toDto(Show show);

    List<ShowResDto> toDtoList(List<Show> shows);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "movie", ignore = true)
    @Mapping(target = "auditorium", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalCapacity", ignore = true)
    @Mapping(target = "availableCapacity", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromDto(ShowReqDto reqDto, @MappingTarget Show show);
}
