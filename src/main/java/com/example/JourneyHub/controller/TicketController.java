package com.example.JourneyHub.controller;

import com.example.JourneyHub.model.dto.MultipleTicketBookingRequest;
import com.example.JourneyHub.model.dto.TicketDto;
import com.example.JourneyHub.service.TicketService;
import jakarta.transaction.Transactional;
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
    public ResponseEntity<?> bookMultipleTickets(@RequestBody List<MultipleTicketBookingRequest> bookingRequests) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        if (bookingRequests == null || bookingRequests.isEmpty()) {
            return ResponseEntity.badRequest().body("Список билетов пуст");
        }

        try {
            List<TicketDto> bookedTickets = ticketService.bookMultipleTickets(email, bookingRequests);
            return ResponseEntity.ok(bookedTickets);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Внутренняя ошибка сервера: " + e.getMessage());
        }
    }

    @PostMapping("/tickets/cancel")
    @Transactional
    public ResponseEntity<?> cancelTickets(@RequestBody MultipleTicketBookingRequest cancelRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        try {
            ticketService.cancelTicketsForRouteOrTrip(email, cancelRequest);
            return ResponseEntity.ok().body("Билеты успешно отменены");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Внутренняя ошибка сервера: " + e.getMessage());
        }
    }
}