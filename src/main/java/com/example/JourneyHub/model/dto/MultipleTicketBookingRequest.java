package com.example.JourneyHub.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultipleTicketBookingRequest {
    @NotEmpty(message = "routeIds is required")
    private List<Long> routeIds;

    @NotNull(message = "seatNumber is required")
    private Integer seatNumber;

    @NotNull(message = "passengerId is required")
    private Long passengerId;
}