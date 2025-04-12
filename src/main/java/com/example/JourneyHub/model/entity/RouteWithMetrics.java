package com.example.JourneyHub.model.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class RouteWithMetrics {
    private final List<Route> path;
    private final int totalPrice;
    private final long totalDuration;
    private final int transfers;

    public RouteWithMetrics(List<Route> path, int totalPrice, long totalDuration, int transfers) {
        this.path = new ArrayList<>(path);
        this.totalPrice = totalPrice;
        this.totalDuration = totalDuration;
        this.transfers = transfers;
    }

    public LocalDateTime getDepartureTime() {
        return path.isEmpty() ? null : path.get(0).getDepartureTime();
    }

    public boolean hasAvailableTickets() {
        return path.stream().allMatch(Route::isHasAvailableTickets);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouteWithMetrics that = (RouteWithMetrics) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}