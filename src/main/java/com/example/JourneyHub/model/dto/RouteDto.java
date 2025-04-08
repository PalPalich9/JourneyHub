package com.example.JourneyHub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RouteDto implements Serializable {
    private Long id;
    private String departureCity;
    private String arrivalCity;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Duration travelDuration;
    private String transportType;
    private boolean hasAvailableTickets;
    private Integer minPrice;
    private Long trip;
    private List<Long> routeIds;
    public RouteDto() {
        this.routeIds = new ArrayList<>();
    }
}