package com.example.JourneyHub.model.entity;

import com.vladmihalcea.hibernate.type.interval.PostgreSQLIntervalType;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.time.Duration;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "departure_city", nullable = false, length = 64)
    private String departureCity;

    @Column(name = "arrival_city", nullable = false, length = 64)
    private String arrivalCity;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "transport_type", nullable = false, length = 64)
    private String transportType;

    @Column(name = "travel_duration", columnDefinition = "INTERVAL")
    @Type(value = PostgreSQLIntervalType.class)
    private Duration travelDuration;

    @Column(name = "has_available_tickets", nullable = false)
    private boolean hasAvailableTickets;

    @Column(name = "min_price", nullable = false)
    private Integer minPrice;
}