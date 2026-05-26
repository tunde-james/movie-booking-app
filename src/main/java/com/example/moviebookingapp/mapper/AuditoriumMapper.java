package com.example.moviebookingapp.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.example.moviebookingapp.dtos.auditorium.AuditoriumReqDto;
import com.example.moviebookingapp.dtos.auditorium.AuditoriumResDto;
import com.example.moviebookingapp.entity.Auditorium;

@Mapper(config = BaseMapperConfig.class)
public interface AuditoriumMapper {

    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "cinema", ignore = true)
    Auditorium toEntity(AuditoriumReqDto req);

    @Mapping(target = "cinemaId", source = "cinema.id")
    AuditoriumResDto toDto(Auditorium auditorium);

    List<AuditoriumResDto> toDtoList(List<Auditorium> auditoriums);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "cinema", ignore = true)
    void updateEntityFromDto(AuditoriumReqDto req, @MappingTarget Auditorium auditorium);
}
