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
        Route route = routeRepository.findById(event.getTicket().getRoute().getId())
                .orElseThrow(() -> new IllegalArgumentException("Маршрут не найден"));
        boolean newHasAvailableTickets = ticketRepository.existsAvailableByRouteId(route.getId());

        if (route.isHasAvailableTickets() != newHasAvailableTickets) {
            route.setHasAvailableTickets(newHasAvailableTickets);
            routeRepository.save(route);

            String routeIndexKey = "route:" + route.getId();
            Set<String> cacheKeys = redisTemplate.opsForSet().members(routeIndexKey).stream()
                    .map(obj -> obj.toString().replaceAll("^\"|\"$", ""))
                    .collect(Collectors.toSet());
            if (!cacheKeys.isEmpty()) {
                redisTemplate.delete(cacheKeys);
                redisTemplate.delete(routeIndexKey);
            }
        }
    }
}