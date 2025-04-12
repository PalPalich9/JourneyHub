package com.example.JourneyHub.controller;

import com.example.JourneyHub.model.dto.MultipleTicketBookingRequest;
import com.example.JourneyHub.model.dto.TicketDto;
import com.example.JourneyHub.service.TicketService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/routes/seats")
    public ResponseEntity<List<Map<String, Object>>> getTicketsGroupedByRoute(
            @RequestParam("routeIds") List<String> routeIdsParam) {
        List<List<Long>> routeIds = routeIdsParam.stream()
                .map(group -> Arrays.stream(group.split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ticketService.getTicketsGroupedByRoute(routeIds));
    }

    @PostMapping("/tickets/book-multiple")
    @Transactional
    public ResponseEntity<List<TicketDto>> bookMultipleTickets(@Valid @RequestBody List<MultipleTicketBookingRequest> bookingRequests) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        List<TicketDto> bookedTickets = ticketService.bookMultipleTickets(email, bookingRequests);
        return ResponseEntity.ok(bookedTickets);
    }

    @PostMapping("/tickets/cancel")
    @Transactional
    public ResponseEntity<String> cancelTickets(@Valid @RequestBody MultipleTicketBookingRequest cancelRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        ticketService.cancelTicketsForRouteOrTrip(email, cancelRequest);
        return ResponseEntity.ok("Билеты успешно отменены");
    }
}