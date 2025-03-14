package com.example.JourneyHub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultipleTicketBookingRequest {
    private Long routeId;
    private Integer seatNumber;
    private Long passengerId;
}