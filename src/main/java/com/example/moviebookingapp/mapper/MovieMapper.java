package com.example.moviebookingapp.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.example.moviebookingapp.dtos.movie.MovieReqDto;
import com.example.moviebookingapp.dtos.movie.MovieResDto;
import com.example.moviebookingapp.entity.Movie;

@Mapper(config = BaseMapperConfig.class)
public interface MovieMapper {

    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "shows", ignore = true)
    Movie toEntity(MovieReqDto req);

    MovieResDto toDto(Movie movie);

    List<MovieResDto> toDtoList(List<Movie> movies);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "shows", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    void updateEntityFromDto(MovieReqDto req, @MappingTarget
    Movie movie);
}
