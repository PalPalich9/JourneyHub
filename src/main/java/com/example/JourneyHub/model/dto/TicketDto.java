package com.example.JourneyHub.model.dto;

import com.example.JourneyHub.model.enums.TicketClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public class TicketDto {
    private Long routeId;
    private Integer seatNumber;
    private boolean isAvailable;
    private TicketClass ticketType;
    private Integer price;
    private String departureCity;
    private String arrivalCity;
    private String transportType;

}
