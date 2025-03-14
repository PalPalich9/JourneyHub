package com.example.JourneyHub.model.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class TicketId implements Serializable {
    private Long route;
    private Integer seatNumber;

    public TicketId() {
    }

    public TicketId(Long route, Integer seatNumber) {
        this.route = route;
        this.seatNumber = seatNumber;
    }
}