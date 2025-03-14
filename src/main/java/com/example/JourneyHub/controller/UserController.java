package com.example.JourneyHub.controller;


import com.example.JourneyHub.model.dto.TicketWithRouteDto;
import com.example.JourneyHub.model.dto.UserDto;

import com.example.JourneyHub.security.CustomUserDetails;
import com.example.JourneyHub.service.TicketService;
import com.example.JourneyHub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final TicketService ticketService;

    @GetMapping("/tickets")
    public ResponseEntity<List<TicketWithRouteDto>> getUserTickets(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "false") boolean showHistory) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long currentUserId = userDetails.getId();
        if (!currentUserId.equals(userId)) {
            throw new SecurityException("У вас нет прав для просмотра билетов другого пользователя");
        }
        List<TicketWithRouteDto> tickets = ticketService.getTicketsWithRouteByUserId(userId, showHistory);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserDto> getUserProfile(@PathVariable Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        Long currentUserId = userDetails.getId();
        if (!currentUserId.equals(userId)) {
            throw new SecurityException("У вас нет прав для просмотра профиля другого пользователя");
        }
        UserDto userProfile = userService.getUserProfile();
        return ResponseEntity.ok(userProfile);
    }
}