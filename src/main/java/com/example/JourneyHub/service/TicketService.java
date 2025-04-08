package com.example.JourneyHub.service;

import com.example.JourneyHub.event.TicketChangeEvent;
import com.example.JourneyHub.model.dto.*;
import com.example.JourneyHub.model.entity.*;
import com.example.JourneyHub.model.mapper.TicketMapper;
import com.example.JourneyHub.repository.PassengerRepository;
import com.example.JourneyHub.repository.RouteRepository;
import com.example.JourneyHub.repository.TicketRepository;
import com.example.JourneyHub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketMapper ticketMapper;
    private final UserRepository userRepository;
    private final PassengerRepository passengerRepository;
    private final RouteRepository routeRepository;
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

        List<TicketDto> bookedTickets = new ArrayList<>();

        for (MultipleTicketBookingRequest request : bookingRequests) {
            bookedTickets.addAll(bookTickets(user, request));
        }

        return bookedTickets;
    }

    private List<TicketDto> bookTickets(User user, MultipleTicketBookingRequest request) {
        List<Ticket> ticketsToBook = ticketRepository.findByRoute_IdIn(request.getRouteIds())
                .stream()
                .filter(t -> t.getSeatNumber().equals(request.getSeatNumber()))
                .collect(Collectors.toList());

        if (ticketsToBook.isEmpty()) {
            throw new IllegalArgumentException("Билеты для указанных маршрутов не найдены");
        }

        if (ticketsToBook.size() != request.getRouteIds().size()) {
            throw new IllegalArgumentException("Не все указанные маршруты имеют билеты с местом " + request.getSeatNumber());
        }

        Long trip = ticketsToBook.get(0).getRoute().getTrip();
        if (!ticketsToBook.stream().allMatch(t -> t.getRoute().getTrip().equals(trip))) {
            throw new IllegalArgumentException("Все маршруты должны относиться к одному trip");
        }

        LocalDateTime now = LocalDateTime.now();
        for (Ticket ticket : ticketsToBook) {
            if (ticket.getPassenger() != null) {
                throw new IllegalStateException("Место " + request.getSeatNumber() +
                        " уже забронировано на маршруте " + ticket.getRoute().getId());
            }
            if (ticket.getRoute().getDepartureTime().isBefore(now.plusMinutes(20))) {
                throw new IllegalStateException("Нельзя бронировать билет менее чем за 20 минут до отправления на маршруте " +
                        ticket.getRoute().getId());
            }
        }

        if (!isPassengerLinkedToUser(user.getEmail(), request.getPassengerId())) {
            throw new IllegalArgumentException("Пассажир не связан с вашим аккаунтом");
        }
        Passenger passenger = passengerRepository.findById(request.getPassengerId())
                .orElseThrow(() -> new IllegalArgumentException("Пассажир не найден"));

        List<TicketDto> result = new ArrayList<>();
        for (Ticket ticket : ticketsToBook) {
            ticket.setPassenger(passenger);
            ticket.setUser(user);
            Ticket savedTicket = ticketRepository.save(ticket);
            eventPublisher.publishEvent(new TicketChangeEvent(this, savedTicket));
            result.add(ticketMapper.toDto(savedTicket));
        }

        return result;
    }

    @Transactional
    public void cancelTicketsForRouteOrTrip(String email, MultipleTicketBookingRequest request) {
        List<Ticket> ticketsToCancel = ticketRepository.findByRoute_IdIn(request.getRouteIds())
                .stream()
                .filter(t -> t.getSeatNumber().equals(request.getSeatNumber()))
                .collect(Collectors.toList());

        if (ticketsToCancel.isEmpty()) {
            throw new IllegalArgumentException("Билеты для указанных маршрутов не найдены");
        }

        if (ticketsToCancel.size() != request.getRouteIds().size()) {
            throw new IllegalArgumentException("Не все указанные билеты найдены для отмены");
        }

        Long trip = ticketsToCancel.get(0).getRoute().getTrip();
        if (!ticketsToCancel.stream().allMatch(t -> t.getRoute().getTrip().equals(trip))) {
            throw new IllegalArgumentException("Все маршруты должны относиться к одному trip");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        LocalDateTime now = LocalDateTime.now();
        for (Ticket ticket : ticketsToCancel) {
            if (ticket.getPassenger() == null) {
                throw new IllegalStateException("Билет на маршруте " + ticket.getRoute().getId() + " не забронирован");
            }
            if (!ticket.getUser().getId().equals(user.getId())) {
                throw new SecurityException("У вас нет прав для отмены билета на маршруте " + ticket.getRoute().getId());
            }
            if (ticket.getRoute().getDepartureTime().isBefore(now)) {
                throw new IllegalStateException("Нельзя отменить билет на маршруте " + ticket.getRoute().getId() +
                        " после отправления");
            }
        }

        for (Ticket ticket : ticketsToCancel) {
            ticket.setPassenger(null);
            ticket.setUser(null);
            Ticket savedTicket = ticketRepository.save(ticket);
            eventPublisher.publishEvent(new TicketChangeEvent(this, savedTicket));
        }
    }

    public List<TicketWithRouteDto> getTicketsWithRouteByUserId(Long userId, boolean showHistory) {
        List<Ticket> tickets = ticketRepository.findTicketsWithRouteByUserId(userId, showHistory);
        return groupTicketsByTrip(tickets);
    }

    public List<TicketWithRouteDto> getTicketsWithRouteByPassengerId(Long passengerId, boolean showHistory) {
        List<Ticket> tickets = ticketRepository.findTicketsWithRouteByPassengerId(passengerId, showHistory);
        return groupTicketsByTrip(tickets);
    }

    private List<TicketWithRouteDto> groupTicketsByTrip(List<Ticket> tickets) {
        Map<String, List<Ticket>> groupedByTripAndSeat = tickets.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getRoute().getTrip() + "_" + t.getSeatNumber(),
                        Collectors.toList()
                ));

        List<TicketWithRouteDto> result = new ArrayList<>();
        for (List<Ticket> ticketGroup : groupedByTripAndSeat.values()) {
            if (ticketGroup.size() == 1) {
                result.add(ticketMapper.toTicketWithRouteDto(ticketGroup.get(0)));
            } else {
                ticketGroup.sort((t1, t2) -> t1.getRoute().getDepartureTime().compareTo(t2.getRoute().getDepartureTime()));
                Ticket first = ticketGroup.get(0);
                Ticket last = ticketGroup.get(ticketGroup.size() - 1);

                long durationSeconds = java.time.Duration.between(
                        first.getRoute().getDepartureTime(),
                        last.getRoute().getArrivalTime()
                ).getSeconds();
                String travelDuration = String.format("%02d:%02d:%02d",
                        durationSeconds / 3600,
                        (durationSeconds % 3600) / 60,
                        durationSeconds % 60);

                result.add(new TicketWithRouteDto(
                        first.getSeatNumber(),
                        ticketGroup.stream().mapToInt(Ticket::getPrice).sum(),
                        first.getTicketType(),
                        first.getUser() != null ? first.getUser().getId() : null,
                        first.getPassenger() != null ? first.getPassenger().getId() : null,
                        first.getRoute().getTrip(),
                        first.getRoute().getDepartureCity(),
                        last.getRoute().getArrivalCity(),
                        first.getRoute().getDepartureTime(),
                        last.getRoute().getArrivalTime(),
                        travelDuration,
                        first.getRoute().getTransportType(),
                        first.getRoute().getTrip(),
                        ticketGroup.stream().map(t -> t.getRoute().getId()).collect(Collectors.toList())
                ));
            }
        }
        return result;
    }

    public List<Map<String, Object>> getTicketsGroupedByRoute(List<List<Long>> routeIds) {
        List<Map<String, Object>> result = new ArrayList<>();

        for (List<Long> routeIdGroup : routeIds) {
            // Проверяем, что переданные routeIds существуют
            List<Route> requestedRoutes = routeRepository.findAllById(routeIdGroup);
            if (requestedRoutes.isEmpty() || requestedRoutes.size() != routeIdGroup.size()) {
                throw new IllegalArgumentException("Некоторые маршруты из группы " + routeIdGroup + " не найдены");
            }

            // Проверяем, что все маршруты в группе относятся к одному trip
            Long trip = requestedRoutes.get(0).getTrip();
            if (!requestedRoutes.stream().allMatch(r -> r.getTrip().equals(trip))) {
                throw new IllegalArgumentException("Все маршруты в группе " + routeIdGroup + " должны относиться к одному trip");
            }

            // Получаем тип транспорта
            String transportType = requestedRoutes.get(0).getTransportType();

            // Получаем билеты только для запрошенных routeIds
            List<Ticket> requestedTickets = ticketRepository.findByRoute_IdIn(routeIdGroup);

            // Группируем запрошенные билеты по номеру места
            Map<Integer, List<Ticket>> ticketsBySeatNumber = requestedTickets.stream()
                    .collect(Collectors.groupingBy(Ticket::getSeatNumber));

            // Формируем список мест
            List<Map<String, Object>> seatsForGroup = new ArrayList<>();
            for (Integer seatNumber : ticketsBySeatNumber.keySet()) {
                List<Ticket> ticketsForSeat = ticketsBySeatNumber.get(seatNumber);

                // Место доступно, только если оно свободно на всех запрошенных routeIds
                boolean isAvailable = ticketsForSeat.stream()
                        .allMatch(ticket -> ticket.getPassenger() == null);

                // Суммируем стоимость всех билетов для этого места в группе
                int totalPrice = ticketsForSeat.stream()
                        .mapToInt(Ticket::getPrice)
                        .sum();

                // Берём ticketType из первого билета (предполагаем, что он одинаков в группе)
                String ticketType = ticketsForSeat.get(0).getTicketType().toString();

                Map<String, Object> seatInfo = new TreeMap<>();
                seatInfo.put("isAvailable", isAvailable);
                seatInfo.put("seatNumber", seatNumber);
                seatInfo.put("price", totalPrice); // Суммированная стоимость
                seatInfo.put("ticketType", ticketType);
                seatsForGroup.add(seatInfo);
            }

            // Сортируем места по номеру
            seatsForGroup.sort(Comparator.comparing(m -> (Integer) m.get("seatNumber")));

            // Формируем результат для группы
            Map<String, Object> groupResult = new TreeMap<>();
            groupResult.put("ticketId", new TicketIdTypeDto(trip, transportType));
            groupResult.put("routeIds", routeIdGroup);
            groupResult.put("seats", seatsForGroup);
            result.add(groupResult);
        }

        return result;
    }
}