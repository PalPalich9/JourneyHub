package com.example.JourneyHub.model.entity;

import com.vladmihalcea.hibernate.type.interval.PostgreSQLIntervalType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "trip", nullable = false)
    private Long trip;

    @Transient
    private List<Long> routeIds = new ArrayList<>();

    public Route() {

    }

    public Route(Long id, String departureCity, String arrivalCity, LocalDateTime departureTime,
                 LocalDateTime arrivalTime, Duration travelDuration, Integer minPrice,
                 String transportType, boolean hasAvailableTickets, Long trip) {
        this.id = id;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.travelDuration = travelDuration;
        this.minPrice = minPrice;
        this.transportType = transportType;
        this.hasAvailableTickets = hasAvailableTickets;
        this.trip = trip;
        if (id != null) {
            this.routeIds.add(id);
        }
    }

    public Route(Long id, String departureCity, String arrivalCity, LocalDateTime departureTime,
                 LocalDateTime arrivalTime, Duration travelDuration, Integer minPrice,
                 String transportType, boolean hasAvailableTickets, Long trip, List<Long> routeIds) {
        this.id = id;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.travelDuration = travelDuration;
        this.minPrice = minPrice;
        this.transportType = transportType;
        this.hasAvailableTickets = hasAvailableTickets;
        this.trip = trip;
        this.routeIds.clear();
        if (routeIds != null) {
            this.routeIds.addAll(routeIds);
        } else if (id != null) {
            this.routeIds.add(id);
        }
    }


    public List<Long> getRouteIds() {
        if (routeIds.isEmpty() && id != null) {
            routeIds.add(id);
        }
        return new ArrayList<>(routeIds);
    }
}