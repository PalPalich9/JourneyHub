package com.example.JourneyHub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultipleTicketBookingRequest {
    private List<Long> routeIds;
    private Integer seatNumber;
    private Long passengerId;
}