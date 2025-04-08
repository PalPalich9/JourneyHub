package com.example.JourneyHub.model.mapper;

import com.example.JourneyHub.model.dto.TicketDto;
import com.example.JourneyHub.model.dto.TicketWithRouteDto;
import com.example.JourneyHub.model.entity.Passenger;
import com.example.JourneyHub.model.entity.Route;
import com.example.JourneyHub.model.entity.Ticket;
import com.example.JourneyHub.model.entity.User;
import com.example.JourneyHub.model.enums.TicketClass;
import java.time.LocalDateTime;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-07T00:57:36+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class TicketMapperImpl implements TicketMapper {

    @Override
    public TicketDto toDto(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }

        Long routeId = null;
        String departureCity = null;
        String arrivalCity = null;
        String transportType = null;
        Integer seatNumber = null;
        TicketClass ticketType = null;
        Integer price = null;

        routeId = ticketRouteId( ticket );
        departureCity = ticketRouteDepartureCity( ticket );
        arrivalCity = ticketRouteArrivalCity( ticket );
        transportType = ticketRouteTransportType( ticket );
        seatNumber = ticket.getSeatNumber();
        ticketType = ticket.getTicketType();
        price = ticket.getPrice();

        boolean isAvailable = ticket.getPassenger() == null;

        TicketDto ticketDto = new TicketDto( routeId, seatNumber, ticketType, price, departureCity, arrivalCity, transportType, isAvailable );

        return ticketDto;
    }

    @Override
    public TicketWithRouteDto toTicketWithRouteDto(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }

        Integer seatNumber = null;
        TicketClass ticketType = null;
        Long userId = null;
        Long passengerId = null;
        Long routeId = null;
        String departureCity = null;
        String arrivalCity = null;
        LocalDateTime departureTime = null;
        LocalDateTime arrivalTime = null;
        String transportType = null;
        Long trip = null;
        Integer price = null;

        seatNumber = ticket.getSeatNumber();
        ticketType = ticket.getTicketType();
        userId = ticketUserId( ticket );
        passengerId = ticketPassengerId( ticket );
        routeId = ticketRouteId( ticket );
        departureCity = ticketRouteDepartureCity( ticket );
        arrivalCity = ticketRouteArrivalCity( ticket );
        departureTime = ticketRouteDepartureTime( ticket );
        arrivalTime = ticketRouteArrivalTime( ticket );
        transportType = ticketRouteTransportType( ticket );
        trip = ticketRouteTrip( ticket );
        price = ticket.getPrice();

        String travelDuration = String.format("%02d:%02d:%02d", ticket.getRoute().getTravelDuration().toHours(), ticket.getRoute().getTravelDuration().toMinutesPart(), ticket.getRoute().getTravelDuration().toSecondsPart());
        List<Long> routeIds = null;

        TicketWithRouteDto ticketWithRouteDto = new TicketWithRouteDto( seatNumber, price, ticketType, userId, passengerId, trip, departureCity, arrivalCity, departureTime, arrivalTime, travelDuration, transportType, routeId, routeIds );

        return ticketWithRouteDto;
    }

    private Long ticketRouteId(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        Route route = ticket.getRoute();
        if ( route == null ) {
            return null;
        }
        Long id = route.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String ticketRouteDepartureCity(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        Route route = ticket.getRoute();
        if ( route == null ) {
            return null;
        }
        String departureCity = route.getDepartureCity();
        if ( departureCity == null ) {
            return null;
        }
        return departureCity;
    }

    private String ticketRouteArrivalCity(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        Route route = ticket.getRoute();
        if ( route == null ) {
            return null;
        }
        String arrivalCity = route.getArrivalCity();
        if ( arrivalCity == null ) {
            return null;
        }
        return arrivalCity;
    }

    private String ticketRouteTransportType(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        Route route = ticket.getRoute();
        if ( route == null ) {
            return null;
        }
        String transportType = route.getTransportType();
        if ( transportType == null ) {
            return null;
        }
        return transportType;
    }

    private Long ticketUserId(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        User user = ticket.getUser();
        if ( user == null ) {
            return null;
        }
        Long id = user.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private Long ticketPassengerId(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        Passenger passenger = ticket.getPassenger();
        if ( passenger == null ) {
            return null;
        }
        Long id = passenger.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private LocalDateTime ticketRouteDepartureTime(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        Route route = ticket.getRoute();
        if ( route == null ) {
            return null;
        }
        LocalDateTime departureTime = route.getDepartureTime();
        if ( departureTime == null ) {
            return null;
        }
        return departureTime;
    }

    private LocalDateTime ticketRouteArrivalTime(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        Route route = ticket.getRoute();
        if ( route == null ) {
            return null;
        }
        LocalDateTime arrivalTime = route.getArrivalTime();
        if ( arrivalTime == null ) {
            return null;
        }
        return arrivalTime;
    }

    private Long ticketRouteTrip(Ticket ticket) {
        if ( ticket == null ) {
            return null;
        }
        Route route = ticket.getRoute();
        if ( route == null ) {
            return null;
        }
        Long trip = route.getTrip();
        if ( trip == null ) {
            return null;
        }
        return trip;
    }
}
