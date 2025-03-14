package com.example.JourneyHub.model.entity;

import com.example.JourneyHub.model.enums.TicketClass;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tickets")
@IdClass(TicketId.class)
public class Ticket {

    @Id
    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Id
    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @ManyToOne
    @JoinColumn(name = "passenger_id")
    private Passenger passenger;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type", nullable = false)
    private TicketClass ticketType;

    @Column(name = "price", nullable = false)
    private Integer price;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}


