package com.example.JourneyHub.model.dto;

import com.example.JourneyHub.model.enums.TicketClass;
import lombok.AllArgsConstructor;
import lombok.Getter;



import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TicketWithRouteDto {
    // Билет
    private Integer seatNumber;
    private Integer price;
    private TicketClass ticketClass;
    private Long userId;
    private Long passengerId;

    // Маршрут
    private Long routeId;
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;

    private String travelDuration;

    private String transportType;
}
