package com.example.JourneyHub.model.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public class TicketIdTypeDto {
    private Long trip;
    private  String transportType;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TicketIdTypeDto that = (TicketIdTypeDto) o;
        return trip.equals(that.trip) && transportType.equals(that.transportType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trip, transportType);
    }
}
