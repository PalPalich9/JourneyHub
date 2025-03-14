package com.example.JourneyHub.model.mapper;

import com.example.JourneyHub.model.dto.UserDto;

import com.example.JourneyHub.model.entity.User;
import com.example.JourneyHub.model.entity.UserPassenger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;


import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "passengerIds", source = "userPassengers", qualifiedByName = "mapPassengerIds")
    UserDto toDto(User user);

    @Mapping(target = "userPassengers", ignore = true)
    User toEntity(UserDto userDto);

    @Named("mapPassengerIds")
    default Set<Long> mapPassengerIds(Set<UserPassenger> userPassengers) {
        if (userPassengers == null) {
            return new HashSet<>();
        }
        return userPassengers.stream()
                .map(up -> up.getPassenger().getId())
                .collect(Collectors.toSet());
    }
}