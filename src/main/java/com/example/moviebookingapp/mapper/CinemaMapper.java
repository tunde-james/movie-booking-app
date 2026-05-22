package com.example.moviebookingapp.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.example.moviebookingapp.dtos.cinema.CinemaReqDto;
import com.example.moviebookingapp.dtos.cinema.CinemaResDto;
import com.example.moviebookingapp.entity.Cinema;

@Mapper(config = BaseMapperConfig.class)
public interface CinemaMapper {

    @Mapping(target = "deleted", ignore = true)
    Cinema toEntity(CinemaReqDto req);

    CinemaResDto toDto(Cinema cinema);

    List<CinemaResDto> toDtoList(List<Cinema> cinemas);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromDto(CinemaReqDto req, @MappingTarget Cinema cinema);
}
