package com.example.JourneyHub.service;

import com.example.JourneyHub.model.dto.RouteDto;
import com.example.JourneyHub.model.entity.RouteWithMetrics;
import com.example.JourneyHub.model.entity.Route;
import com.example.JourneyHub.model.enums.SortCriteria;
import com.example.JourneyHub.model.mapper.RouteMapper;
import com.example.JourneyHub.repository.RouteRepository;
import com.example.JourneyHub.utils.CacheUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;



@Service
@RequiredArgsConstructor
public class RouteService {

        private final RouteRepository routeRepository;
        private final RouteMapper routeMapper;
        private final ObjectMapper objectMapper;
        private final RedisTemplate<String, Object> redisTemplate;

        private static final int MAX_TOTAL_PATHS = 50;
        private static final long MAX_TOTAL_DURATION_MINUTES = 48 * 60;

        @Cacheable(value = "routesSearch", key = "#departureCity + ',' + #arrivalCity + ',' + #startTime + ',' + #directOnly + ',' + #multiStop + ',' + (#sort != null ? #sort.name() : 'DEFAULT') + ',' + (#transportType != null ? #transportType : 'null')")
        @Transactional(readOnly = true)
        public List<List<RouteDto>> findRoutes(String departureCity, String arrivalCity,
                                               LocalDateTime startTime, SortCriteria sort,
                                               String transportType, boolean directOnly,
                                               boolean multiStop) {

                LocalDateTime maxStartDayTime = startTime.toLocalDate().atTime(23, 59, 59);
                LocalDateTime maxDepartureTime = startTime.toLocalDate().plusDays(2).atTime(23, 59, 59);

                List<Route> directRoutes = findDirectRoutes(departureCity, arrivalCity, startTime, maxStartDayTime, transportType);
                List<List<Route>> paths = new ArrayList<>();
                Set<Long> directRouteIds = directRoutes.stream().map(Route::getId).collect(Collectors.toSet());
                paths.addAll(directRoutes.stream().map(Collections::singletonList).collect(Collectors.toList()));

                if (!directOnly && paths.size() < MAX_TOTAL_PATHS) {
                        Map<String, List<Route>> routesByDepartureCity = indexRoutes(startTime, maxDepartureTime, arrivalCity, transportType);

                        List<Route> startingRoutes = routesByDepartureCity.getOrDefault(departureCity, Collections.emptyList())
                                .stream()
                                .filter(route -> route.getDepartureTime().isAfter(startTime) || route.getDepartureTime().isEqual(startTime))
                                .filter(route -> !route.getDepartureTime().isAfter(maxStartDayTime))
                                .filter(route -> !directRouteIds.contains(route.getId()))
                                .collect(Collectors.toList());

                        for (Route startRoute : startingRoutes) {
                                Set<String> visitedCities = new HashSet<>();
                                visitedCities.add(startRoute.getDepartureCity());
                                buildPathsWithTransfers(startRoute, arrivalCity, new ArrayList<>(List.of(startRoute)), visitedCities, paths, routesByDepartureCity, startTime, 1, multiStop);
                                if (paths.size() >= MAX_TOTAL_PATHS) {
                                        break;
                                }
                        }
                }

                List<RouteWithMetrics> pathsWithMetrics = paths.stream()
                        .map(path -> {
                                int totalPrice = calculateTotalPrice(path);
                                long totalDuration = calculateTotalDuration(path);
                                int transfers = path.size() - 1;
                                return new RouteWithMetrics(path, totalPrice, totalDuration, transfers);
                        })
                        .collect(Collectors.toList());

                pathsWithMetrics.sort((p1, p2) -> {
                        int comparison;
                        switch (sort != null ? sort : SortCriteria.DEFAULT) {
                                case CHEAPEST:
                                        comparison = Integer.compare(p1.getTotalPrice(), p2.getTotalPrice());
                                        break;
                                case EXPENSIVE:
                                        comparison = Integer.compare(p2.getTotalPrice(), p1.getTotalPrice());
                                        break;
                                case DURATION:
                                        comparison = Long.compare(p1.getTotalDuration(), p2.getTotalDuration());
                                        break;
                                case AVAILABILITY:
                                        boolean allTickets1 = p1.getPath().stream().allMatch(Route::isHasAvailableTickets);
                                        boolean allTickets2 = p2.getPath().stream().allMatch(Route::isHasAvailableTickets);
                                        if (allTickets1 && !allTickets2) comparison = -1;
                                        else if (!allTickets1 && allTickets2) comparison = 1;
                                        else comparison = 0;
                                        break;
                                case DEFAULT:
                                        comparison = p1.getPath().get(0).getDepartureTime().compareTo(p2.getPath().get(0).getDepartureTime());
                                        break;
                                default:
                                        comparison = 0;
                                        break;
                        }
                        if (comparison != 0) {
                                return comparison;
                        }
                        return Integer.compare(p1.getTransfers(), p2.getTransfers());
                });

                List<List<RouteDto>> result = pathsWithMetrics.stream()
                        .limit(MAX_TOTAL_PATHS)
                        .map(RouteWithMetrics::getPath)
                        .map(path -> path.stream().map(routeMapper::toDto).collect(Collectors.toList()))
                        .collect(Collectors.toList());

                String cacheKey = "routesSearch::" + departureCity + "," + arrivalCity + "," + startTime + "," + directOnly + "," + multiStop + "," + (sort != null ? sort.name() : "DEFAULT") + "," + (transportType != null ? transportType : "null");
                CacheUtils.updateRouteIndex(result, redisTemplate, objectMapper, cacheKey);

                return result;
        }

        private void buildPathsWithTransfers(Route currentRoute, String arrivalCity, List<Route> currentPath,
                                             Set<String> visitedCities, List<List<Route>> paths,
                                             Map<String, List<Route>> routesByDepartureCity, LocalDateTime startTime,
                                             int currentTransfers, boolean multiStop) {

                long totalDurationMinutes = calculateTotalDuration(currentPath);
                if (totalDurationMinutes > MAX_TOTAL_DURATION_MINUTES) {
                        return;
                }

                if (currentRoute.getArrivalCity().equals(arrivalCity)) {
                        paths.add(new ArrayList<>(currentPath));
                        if (paths.size() >= MAX_TOTAL_PATHS) {
                                return;
                        }
                        return;
                }

                int maxSegments = multiStop ? 4 : 2;
                if (currentPath.size() >= maxSegments) {
                        return;
                }

                if (visitedCities.contains(currentRoute.getArrivalCity())) {
                        return;
                }

                visitedCities.add(currentRoute.getArrivalCity());

                List<Route> nextRoutes = routesByDepartureCity.getOrDefault(currentRoute.getArrivalCity(), Collections.emptyList());
                for (Route nextRoute : nextRoutes) {
                        long waitingTimeMinutes = ChronoUnit.MINUTES.between(currentRoute.getArrivalTime(), nextRoute.getDepartureTime());
                        long minWaitingTime = currentRoute.getTransportType().equals(nextRoute.getTransportType()) ? 60 : 180;
                        long maxWaitingTime = 25 * 60;

                        if (waitingTimeMinutes >= minWaitingTime && waitingTimeMinutes <= maxWaitingTime) {
                                currentPath.add(nextRoute);
                                buildPathsWithTransfers(nextRoute, arrivalCity, currentPath, visitedCities, paths, routesByDepartureCity, startTime, currentTransfers + 1, multiStop);
                                currentPath.remove(currentPath.size() - 1);
                        }
                }

                visitedCities.remove(currentRoute.getArrivalCity());
        }

        private List<Route> findDirectRoutes(String departureCity, String arrivalCity, LocalDateTime startTime,
                                             LocalDateTime maxDepartureTime, String transportType) {
                List<Route> directRoutes = routeRepository.findDirectRoutes(startTime, maxDepartureTime, departureCity, arrivalCity, transportType)
                        .stream()
                        .filter(route -> route.getDepartureTime().isAfter(startTime) || route.getDepartureTime().isEqual(startTime))
                        .filter(route -> !route.getDepartureTime().isAfter(maxDepartureTime))
                        .collect(Collectors.toList());

                if (transportType == null || "mix".equalsIgnoreCase(transportType)) {
                        return directRoutes;
                }
                return directRoutes.stream()
                        .filter(route -> transportType.equalsIgnoreCase(route.getTransportType()))
                        .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        private Map<String, List<Route>> indexRoutes(LocalDateTime minDepartureTime, LocalDateTime maxDepartureTime,
                                                     String arrivalCity, String transportType) {
                Map<String, List<Route>> routesByDepartureCity = new HashMap<>();

                try (Stream<Route> routeStream = routeRepository.streamRoutesInTimeRange(minDepartureTime, maxDepartureTime, arrivalCity, transportType)) {
                        Stream<Route> filteredStream = (transportType == null || "mix".equalsIgnoreCase(transportType))
                                ? routeStream
                                : routeStream.filter(route -> transportType.equalsIgnoreCase(route.getTransportType()));
                        filteredStream.forEach(route -> {
                                String departureCity = route.getDepartureCity();
                                routesByDepartureCity.computeIfAbsent(departureCity, k -> new ArrayList<>()).add(route);
                        });
                }

                return routesByDepartureCity;
        }

        private int calculateTotalPrice(List<Route> path) {
                return path.stream().mapToInt(Route::getMinPrice).sum();
        }

        private long calculateTotalDuration(List<Route> path) {
                long totalTravelDuration = path.stream()
                        .mapToLong(route -> route.getTravelDuration().toMinutes())
                        .sum();
                long waitingTime = 0;
                for (int i = 0; i < path.size() - 1; i++) {
                        waitingTime += ChronoUnit.MINUTES.between(path.get(i).getArrivalTime(), path.get(i + 1).getDepartureTime());
                }
                return totalTravelDuration + waitingTime;
        }

        @Cacheable(value = "routesDirect", key = "{#departureCity, #arrivalCity, #transportType, #sortCriteria}")
        public Map<LocalDate, List<RouteDto>> getDirectRoutesGroupedByDate(String departureCity, String arrivalCity,
                                                                           String transportType, SortCriteria sortCriteria) {
                LocalDateTime startDate = LocalDateTime.now();
                LocalDateTime endDate = startDate.plusDays(14);
                String sortCriteriaStr = sortCriteria != null ? sortCriteria.name() : SortCriteria.DEFAULT.name();

                List<RouteDto> routes = routeRepository.findDirectRoutesWithSort(departureCity, arrivalCity, startDate, endDate, transportType, sortCriteriaStr)
                        .stream()
                        .map(routeMapper::toDto)
                        .collect(Collectors.toList());

                String cacheKey = "routesDirect::" + departureCity + "," + arrivalCity + "," + transportType + "," + sortCriteriaStr;
                CacheUtils.updateRouteIndex(Collections.singletonList(routes), redisTemplate, objectMapper, cacheKey);

                return routes.stream()
                        .collect(Collectors.groupingBy(
                                route -> route.getDepartureTime().toLocalDate(),
                                TreeMap::new,
                                Collectors.toList()
                        ));
        }
}