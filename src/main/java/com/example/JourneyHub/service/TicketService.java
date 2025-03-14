package com.example.JourneyHub.service;

import com.example.JourneyHub.event.TicketChangeEvent;
import com.example.JourneyHub.model.dto.MultipleTicketBookingRequest;
import com.example.JourneyHub.model.dto.TicketDto;
import com.example.JourneyHub.model.dto.TicketIdTypeDto;
import com.example.JourneyHub.model.dto.TicketWithRouteDto;
import com.example.JourneyHub.model.entity.Passenger;
import com.example.JourneyHub.model.entity.Ticket;
import com.example.JourneyHub.model.entity.TicketId;
import com.example.JourneyHub.model.entity.User;
import com.example.JourneyHub.model.mapper.TicketMapper;
import com.example.JourneyHub.repository.PassengerRepository;
import com.example.JourneyHub.repository.TicketRepository;
import com.example.JourneyHub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final UserRepository userRepository;
    private final PassengerRepository passengerRepository;
    private final ApplicationEventPublisher eventPublisher;



    public boolean isPassengerLinkedToUser(String email, Long passengerId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return user.getUserPassengers().stream()
                .anyMatch(up -> up.getPassenger().getId().equals(passengerId));
    }



    @Transactional
    public List<TicketDto> bookMultipleTickets(String email, List<MultipleTicketBookingRequest> bookingRequests) {
        if (bookingRequests == null || bookingRequests.isEmpty()) {
            throw new IllegalArgumentException("Список билетов пуст");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        for (MultipleTicketBookingRequest request : bookingRequests) {
            TicketId ticketId = new TicketId(request.getRouteId(), request.getSeatNumber());
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new IllegalArgumentException("Билет не найден для routeId: " + request.getRouteId() + ", место: " + request.getSeatNumber()));

            if (ticket.getPassenger() != null) {
                throw new IllegalStateException("Билет(ы) забронировал(и)");
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime departureTime = ticket.getRoute().getDepartureTime();
            if (departureTime.isBefore(now.plusMinutes(20))) {
                throw new IllegalStateException("Нельзя забронировать билет менее чем за 20 минут до отправления");
            }

            Long passengerId = request.getPassengerId();
            if (!isPassengerLinkedToUser(email, passengerId)) {
                throw new IllegalArgumentException("Укажите пассажира, связанного с вашим аккаунтом, для всех билетов");
            }
        }

            List<TicketDto> bookedTickets = bookingRequests.stream().map(request -> {
            TicketId ticketId = new TicketId(request.getRouteId(), request.getSeatNumber());
            Ticket ticket = ticketRepository.findById(ticketId)
                    .orElseThrow(() -> new IllegalArgumentException("Билет не найден"));

            Passenger passenger = passengerRepository.findById(request.getPassengerId())
                    .orElseThrow(() -> new IllegalArgumentException("Пассажир не найден"));

            ticket.setPassenger(passenger);
            ticket.setUser(user);
            Ticket savedTicket = ticketRepository.save(ticket);

            eventPublisher.publishEvent(new TicketChangeEvent(this, savedTicket));
            return ticketMapper.toDto(savedTicket);
        }).collect(Collectors.toList());

        return bookedTickets;
    }

    @Transactional
    public void cancelTicket(Long routeId, Integer seatNumber) {
        Ticket ticket = ticketRepository.findById(new TicketId(routeId, seatNumber))
                .orElseThrow(() -> new IllegalArgumentException("Билет не найден"));
        ticket.setPassenger(null);
        ticket.setUser(null);
        Ticket savedTicket = ticketRepository.save(ticket);

        eventPublisher.publishEvent(new TicketChangeEvent(this, savedTicket));
    }

    public boolean canCancelTicket(String email, Long routeId, Integer seatNumber) {
        Ticket ticket = ticketRepository.findById(new TicketId(routeId, seatNumber))
                .orElseThrow(() -> new IllegalArgumentException("Билет не найден"));

        if (ticket.getPassenger() == null) {
            return false;
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        if (ticket.getUser() == null || !ticket.getUser().getId().equals(user.getId())) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departureTime = ticket.getRoute().getDepartureTime();
        if (departureTime.isBefore(now)) {
            return false;
        }

        return true;
    }



    public List<TicketWithRouteDto> getTicketsWithRouteByUserId(Long userId, boolean showHistory) {
        return ticketRepository.findTicketsWithRouteByUserId(userId, showHistory).stream()
                .map(ticketMapper::toTicketWithRouteDto)
                .collect(Collectors.toList());
    }

    public List<TicketWithRouteDto> getTicketsWithRouteByPassengerId(Long passengerId, boolean showHistory) {
        return ticketRepository.findTicketsWithRouteByPassengerId(passengerId, showHistory).stream()
                .map(ticketMapper::toTicketWithRouteDto)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getTicketsGroupedByRoute(List<Long> routeIds) {
        Map<TicketIdTypeDto, List<TicketDto>> groupedTickets = ticketRepository.findByRoute_IdIn(routeIds).stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.groupingBy(ticketDto -> new TicketIdTypeDto(ticketDto.getRouteId(), ticketDto.getTransportType())));

        return groupedTickets.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new TreeMap<>();
                    item.put("ticketId", new TicketIdTypeDto(entry.getKey().getRouteId(), entry.getKey().getTransportType()));
                    item.put("tickets", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());
    }
}