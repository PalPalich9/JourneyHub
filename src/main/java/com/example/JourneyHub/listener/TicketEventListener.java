package com.example.JourneyHub.listener;

import com.example.JourneyHub.event.TicketChangeEvent;
import com.example.JourneyHub.model.entity.Route;
import com.example.JourneyHub.model.entity.Ticket;
import com.example.JourneyHub.repository.RouteRepository;
import com.example.JourneyHub.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TicketEventListener {

    private final RouteRepository routeRepository;
    private final TicketRepository ticketRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @EventListener
    @Transactional
    public void handleTicketChange(TicketChangeEvent event) {
        Ticket ticket = event.getTicket();
        Long routeId = ticket.getRoute().getId();

        boolean hasAvailableTickets = ticketRepository.existsAvailableByRouteId(routeId);
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Маршрут не найден"));
        route.setHasAvailableTickets(hasAvailableTickets);
        routeRepository.save(route);

        String routeIndexKey = "route:" + routeId;
        Set<Object> cacheKeysRaw = redisTemplate.opsForSet().members(routeIndexKey);
        if (cacheKeysRaw != null && !cacheKeysRaw.isEmpty()) {
            Set<String> cacheKeys = cacheKeysRaw.stream()
                    .map(obj -> obj.toString().replaceAll("^\"|\"$", ""))
                    .collect(Collectors.toSet());
            redisTemplate.delete(cacheKeys);
            redisTemplate.delete(routeIndexKey);
        }
    }
}