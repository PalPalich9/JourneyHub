package com.example.JourneyHub.model.dto;

import com.example.JourneyHub.model.enums.TicketClass;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@Getter
@AllArgsConstructor
public class TicketDto {
    private Long routeId;
    private Integer seatNumber;
    private TicketClass ticketType;
    private Integer price;
    private String departureCity;
    private String arrivalCity;
    private String transportType;
    private boolean isAvailable;
}
