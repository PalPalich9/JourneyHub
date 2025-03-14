package com.example.JourneyHub.model.mapper;

import com.example.JourneyHub.model.dto.RouteDto;
import com.example.JourneyHub.model.entity.Route;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface RouteMapper {
    @Mapping(target = "minPrice", source = "minPrice")
    @Mapping(target = "departureTime", source = "departureTime")
    RouteDto toDto(Route route);
}