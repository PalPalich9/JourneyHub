package com.example.JourneyHub.model.entity;

import lombok.Data;

import java.util.List;

@Data
public class RouteWithMetrics {
    private final List<Route> path;
    private final int totalPrice;
    private final long totalDuration;
    private final int transfers;

    public RouteWithMetrics(List<Route> path, int totalPrice, long totalDuration, int transfers) {
        this.path = path;
        this.totalPrice = totalPrice;
        this.totalDuration = totalDuration;
        this.transfers = transfers;
    }
}
