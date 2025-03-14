package com.example.JourneyHub.controller;

import com.example.JourneyHub.model.dto.PassengerCreationDto;
import com.example.JourneyHub.model.dto.PassengerDto;
import com.example.JourneyHub.model.dto.TicketWithRouteDto;
import com.example.JourneyHub.service.PassengerService;
import com.example.JourneyHub.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/passengers")
@RequiredArgsConstructor
public class PassengerController {

    private final PassengerService passengerService;
    private final TicketService ticketService;


    @PostMapping
    public ResponseEntity<PassengerDto> addPassenger(@PathVariable Long userId, @RequestBody PassengerCreationDto passengerDto) {
        PassengerDto passenger = passengerService.addPassenger(userId, passengerDto);
        return ResponseEntity.ok(passenger);
    }
    @GetMapping("/{passengerId}/tickets")
    public ResponseEntity<List<TicketWithRouteDto>> getTicketsByPassenger(
            @PathVariable Long userId,
            @PathVariable Long passengerId,
            @RequestParam(defaultValue = "false") boolean showHistory) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        if (!ticketService.isPassengerLinkedToUser(email, passengerId)) {
            return ResponseEntity.notFound().build();
        }
        List<TicketWithRouteDto> tickets = ticketService.getTicketsWithRouteByPassengerId(passengerId, showHistory);
        return ResponseEntity.ok(tickets);
    }
    @GetMapping
    public ResponseEntity<List<PassengerDto>> getPassengers(@PathVariable Long userId) {
        List<PassengerDto> passengers = passengerService.getPassengersByUser(userId);
        return ResponseEntity.ok(passengers);
    }
    @GetMapping("/{passengerId}")
    public ResponseEntity<PassengerDto> getPassenger(@PathVariable Long userId, @PathVariable Long passengerId) {
        PassengerDto passenger = passengerService.getPassengerByUserAndId(userId, passengerId);
        return ResponseEntity.ok(passenger);
    }
    @DeleteMapping("/{passengerId}")
    public ResponseEntity<Void> removePassenger(@PathVariable Long userId, @PathVariable Long passengerId) {
        passengerService.removePassenger(userId, passengerId);
        return ResponseEntity.noContent().build();
    }
}
