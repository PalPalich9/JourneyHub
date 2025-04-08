package com.example.JourneyHub.model.dto;

import com.example.JourneyHub.model.enums.TicketClass;
import lombok.AllArgsConstructor;
import lombok.Getter;



import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class TicketWithRouteDto {
    private Integer seatNumber;
    private Integer price;
    private TicketClass ticketType;
    private Long userId;
    private Long passengerId;
    private Long trip;
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String travelDuration;
    private String transportType;
    private Long routeId;
    private List<Long> routeIds;
}
