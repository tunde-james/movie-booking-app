package com.example.moviebookingapp.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.example.moviebookingapp.dtos.show.ShowReqDto;
import com.example.moviebookingapp.dtos.show.ShowResDto;
import com.example.moviebookingapp.entity.Show;

@Mapper(
        config = BaseMapperConfig.class,
        uses = {MovieMapper.class, CinemaMapper.class})
public interface ShowMapper {

    @Mapping(target = "movie", ignore = true)
    @Mapping(target = "cinema", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "availableSeats", source = "totalSeats")
    @Mapping(target = "deleted", ignore = true)
    Show toEntity(ShowReqDto req);

    @Mapping(target = "showStatus", source = "status")
    ShowResDto toDto(Show show);

    List<ShowResDto> toDtoList(List<Show> shows);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "movie", ignore = true)
    @Mapping(target = "cinema", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "availableSeats", ignore = true)
    void updateEntityFromDto(ShowReqDto req, @MappingTarget Show show);
}
