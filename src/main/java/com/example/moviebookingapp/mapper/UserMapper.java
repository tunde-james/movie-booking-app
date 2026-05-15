package com.example.moviebookingapp.mapper;

import java.util.List;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.example.moviebookingapp.dtos.user.UserReqDto;
import com.example.moviebookingapp.dtos.user.UserResDto;
import com.example.moviebookingapp.entity.User;

@Mapper(config = BaseMapperConfig.class)
public interface UserMapper {

    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "password", ignore = true)
    User toEntity(UserReqDto req);

    UserResDto toDto(User user);

    List<UserResDto> toDtoList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntityFromDto(UserReqDto req, @MappingTarget
    User user);

}
