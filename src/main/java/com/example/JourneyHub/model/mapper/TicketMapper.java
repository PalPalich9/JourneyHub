package com.example.JourneyHub.model.mapper;

import com.example.JourneyHub.model.dto.TicketDto;
import com.example.JourneyHub.model.dto.TicketWithRouteDto;
import com.example.JourneyHub.model.entity.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "departureCity", source = "route.departureCity")
    @Mapping(target = "arrivalCity", source = "route.arrivalCity")
    @Mapping(target = "transportType", source = "route.transportType")
    @Mapping(target = "isAvailable", expression = "java(ticket.getPassenger() == null)")
    TicketDto toDto(Ticket ticket);

    @Mapping(target = "seatNumber", source = "seatNumber")
    @Mapping(target = "ticketType", source = "ticketType")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "passengerId", source = "passenger.id")
    @Mapping(target = "routeId", source = "route.id")
    @Mapping(target = "departureCity", source = "route.departureCity")
    @Mapping(target = "arrivalCity", source = "route.arrivalCity")
    @Mapping(target = "departureTime", source = "route.departureTime")
    @Mapping(target = "arrivalTime", source = "route.arrivalTime")
    @Mapping(target = "travelDuration", expression = "java(String.format(\"%02d:%02d:%02d\", ticket.getRoute().getTravelDuration().toHours(), ticket.getRoute().getTravelDuration().toMinutesPart(), ticket.getRoute().getTravelDuration().toSecondsPart()))")    @Mapping(target = "transportType", source = "route.transportType")
    @Mapping(target = "trip", source = "route.trip")
    TicketWithRouteDto toTicketWithRouteDto(Ticket ticket);
}