package com.example.JourneyHub.controller;

import com.example.JourneyHub.model.dto.RouteDto;
import com.example.JourneyHub.model.enums.SortCriteria;
import com.example.JourneyHub.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;


    @GetMapping("/direct-by-date")
    public ResponseEntity<Map<LocalDate, List<RouteDto>>> getDirectRoutesGroupedByDate(
            @RequestParam String departureCity,
            @RequestParam String arrivalCity,
            @RequestParam(required = false) String transportType,
            @RequestParam(defaultValue = "DEFAULT") SortCriteria sort) {
        return ResponseEntity.ok(routeService.getDirectRoutesGroupedByDate(departureCity, arrivalCity, transportType, sort));
    }

    @GetMapping("/search")
    public List<List<RouteDto>> searchRoutes(
            @RequestParam String departureCity,
            @RequestParam String arrivalCity,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(defaultValue = "DEFAULT") SortCriteria sort,
            @RequestParam(required = false) String transportType,
            @RequestParam(defaultValue = "false") boolean directOnly,
            @RequestParam(defaultValue = "false") boolean multiStop) {
        return routeService.findRoutes(departureCity, arrivalCity, startTime, sort, transportType, directOnly, multiStop);
    }
}