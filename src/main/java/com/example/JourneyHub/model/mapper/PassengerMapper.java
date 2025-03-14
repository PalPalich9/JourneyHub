package com.example.JourneyHub.model.mapper;

import com.example.JourneyHub.model.dto.PassengerCreationDto;
import com.example.JourneyHub.model.dto.PassengerDto;
import com.example.JourneyHub.model.entity.Passenger;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PassengerMapper {
    PassengerDto toDto(Passenger passenger);

    @Mapping(target = "id", ignore = true)
    Passenger toEntity(PassengerCreationDto passengerCreationDto);
}