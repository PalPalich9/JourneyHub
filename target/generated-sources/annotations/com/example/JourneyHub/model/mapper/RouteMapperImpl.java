package com.example.JourneyHub.model.mapper;

import com.example.JourneyHub.model.dto.RouteDto;
import com.example.JourneyHub.model.entity.Route;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-12T01:17:56+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class RouteMapperImpl implements RouteMapper {

    @Override
    public RouteDto toDto(Route route) {
        if ( route == null ) {
            return null;
        }

        RouteDto routeDto = new RouteDto();

        routeDto.setMinPrice( route.getMinPrice() );
        routeDto.setDepartureTime( route.getDepartureTime() );
        List<Long> list = route.getRouteIds();
        if ( list != null ) {
            routeDto.setRouteIds( new ArrayList<Long>( list ) );
        }
        routeDto.setId( route.getId() );
        routeDto.setDepartureCity( route.getDepartureCity() );
        routeDto.setArrivalCity( route.getArrivalCity() );
        routeDto.setArrivalTime( route.getArrivalTime() );
        routeDto.setTravelDuration( route.getTravelDuration() );
        routeDto.setTransportType( route.getTransportType() );
        routeDto.setHasAvailableTickets( route.isHasAvailableTickets() );
        routeDto.setTrip( route.getTrip() );

        return routeDto;
    }
}
