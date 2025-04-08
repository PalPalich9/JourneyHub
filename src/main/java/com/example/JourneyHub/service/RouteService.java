package com.example.JourneyHub.service;

import com.example.JourneyHub.model.dto.RouteDto;
import com.example.JourneyHub.model.entity.Route;
import com.example.JourneyHub.model.entity.RouteWithMetrics;
import com.example.JourneyHub.model.enums.SortCriteria;
import com.example.JourneyHub.model.mapper.RouteMapper;
import com.example.JourneyHub.repository.RouteRepository;
import com.example.JourneyHub.utils.CacheUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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
        private static final long MAX_WAITING_TIME_MINUTES = 25 * 60;

        @Cacheable(value = "routesSearch", key = "#departureCity + ',' + #arrivalCity + ',' + #startTime + ',' + #directOnly + ',' + #multiStop + ',' + (#sort != null ? #sort.name() : 'DEFAULT') + ',' + (#transportType != null ? #transportType : 'null')")
        @Transactional(readOnly = true)
        public List<List<RouteDto>> findRoutes(String departureCity, String arrivalCity,
                                               LocalDateTime startTime, SortCriteria sort,
                                               String transportType, boolean directOnly, boolean multiStop) {
                LocalDateTime maxDepartureTimeForStartCity = startTime.toLocalDate().atTime(23, 59, 59);
                LocalDateTime maxDepartureTime = startTime.toLocalDate().plusDays(2).atTime(23, 59, 59);
                int maxTransfers = directOnly ? 0 : (multiStop ? 3 : 1);

                String resolvedTransportType = resolveTransportType(transportType);

                List<Route> directRoutes = findDirectRoutesAndTrips(departureCity, arrivalCity, startTime, maxDepartureTimeForStartCity, resolvedTransportType, directOnly);
                Map<String, List<Route>> routesByDepartureCity = directOnly ? new HashMap<>() : indexRoutes(startTime, maxDepartureTime, arrivalCity, resolvedTransportType, departureCity, maxDepartureTimeForStartCity);

                System.out.println("Direct routes: " + directRoutes);
                System.out.println("Routes by departure city (filtered for Братск and Красноярск): " +
                        routesByDepartureCity.entrySet().stream()
                                .filter(entry -> entry.getKey().equals("Братск") || entry.getKey().equals("Красноярск"))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

                Comparator<RouteWithMetrics> comparator = getComparator(sort);
                PriorityQueue<RouteWithMetrics> bestPaths = new PriorityQueue<>(MAX_TOTAL_PATHS + 1, comparator);
                Set<RouteWithMetrics> uniquePaths = new HashSet<>();

                directRoutes.forEach(route -> {
                        if (route.getArrivalCity().equals(arrivalCity)) {
                                RouteWithMetrics metrics = new RouteWithMetrics(
                                        Collections.singletonList(route), route.getMinPrice(), calculateTotalDuration(Collections.singletonList(route)), 0);
                                if (uniquePaths.add(metrics)) {
                                        bestPaths.add(metrics);
                                        if (bestPaths.size() > MAX_TOTAL_PATHS) bestPaths.poll();
                                }
                        }
                });

                if (!directOnly) {
                        Map<String, Set<RouteWithMetrics>> visitedPathsByCity = new HashMap<>();
                        visitedPathsByCity.put(departureCity, Collections.singleton(new RouteWithMetrics(Collections.emptyList(), 0, 0, 0)));

                        for (Route route : directRoutes) {
                                if (!route.getArrivalCity().equals(arrivalCity)) {
                                        List<Route> initialPath = Collections.singletonList(route);
                                        RouteWithMetrics initialMetrics = new RouteWithMetrics(
                                                initialPath, route.getMinPrice(), calculateTotalDuration(initialPath), 0);
                                        String nextCity = route.getArrivalCity();
                                        visitedPathsByCity.computeIfAbsent(nextCity, k -> new HashSet<>()).add(initialMetrics);
                                }
                        }

                        List<Route> allRoutes = routeRepository.findRoutesAndTrips(departureCity, arrivalCity, startTime, maxDepartureTime, resolvedTransportType);
                        Map<Long, List<Route>> routesByTrip = groupRoutesByTrip(allRoutes.stream().collect(Collectors.groupingBy(Route::getDepartureCity)));

                        for (int round = 0; round < maxTransfers && bestPaths.size() < MAX_TOTAL_PATHS; round++) {
                                Map<String, List<RouteWithMetrics>> newPathsByCity = new HashMap<>();
                                boolean improved = false;

                                for (String currentCity : new ArrayList<>(visitedPathsByCity.keySet())) {
                                        Set<RouteWithMetrics> currentPaths = visitedPathsByCity.get(currentCity);
                                        List<Route> possibleRoutes = routesByDepartureCity.getOrDefault(currentCity, Collections.emptyList());

                                        for (Route route : possibleRoutes) {
                                                LocalDateTime maxTime = currentCity.equals(departureCity) ? maxDepartureTimeForStartCity : maxDepartureTime;
                                                if (route.getDepartureTime().isBefore(startTime) || route.getDepartureTime().isAfter(maxTime)) continue;

                                                for (RouteWithMetrics prevPath : currentPaths) {
                                                        long waitingTime = prevPath.getPath().isEmpty()
                                                                ? 0
                                                                : ChronoUnit.MINUTES.between(prevPath.getPath().get(prevPath.getPath().size() - 1).getArrivalTime(), route.getDepartureTime());

                                                        if (!isValidTransfer(prevPath, route, waitingTime)) continue;

                                                        List<Route> newPath = new ArrayList<>(prevPath.getPath());
                                                        Route mergedRoute = route;

                                                        if (!newPath.isEmpty() && newPath.get(newPath.size() - 1).getTrip().equals(route.getTrip())) {
                                                                Route lastRoute = newPath.remove(newPath.size() - 1);
                                                                List<Route> segmentsToMerge = new ArrayList<>(Arrays.asList(lastRoute, route));
                                                                List<Route> tripSegments = routesByTrip.getOrDefault(route.getTrip(), segmentsToMerge);
                                                                tripSegments.sort(Comparator.comparing(Route::getDepartureTime));
                                                                int lastIndex = tripSegments.indexOf(lastRoute);
                                                                int routeIndex = tripSegments.indexOf(route);
                                                                if (lastIndex != -1 && routeIndex != -1 && lastIndex < routeIndex) {
                                                                        List<Route> validSegments = new ArrayList<>();
                                                                        for (int i = lastIndex; i <= routeIndex; i++) {
                                                                                validSegments.add(tripSegments.get(i));
                                                                        }
                                                                        mergedRoute = mergeTripRoutes(validSegments);
                                                                } else {
                                                                        mergedRoute = mergeTripRoutes(segmentsToMerge);
                                                                }
                                                        } else {
                                                                List<Route> tripSegments = routesByTrip.getOrDefault(route.getTrip(), Collections.singletonList(route));
                                                                tripSegments.sort(Comparator.comparing(Route::getDepartureTime));
                                                                int routeIndex = tripSegments.indexOf(route);
                                                                if (routeIndex != -1) {
                                                                        List<Route> validSegments = new ArrayList<>();
                                                                        validSegments.add(route);
                                                                        for (int i = routeIndex + 1; i < tripSegments.size(); i++) {
                                                                                Route nextSegment = tripSegments.get(i);
                                                                                long segmentWaiting = ChronoUnit.MINUTES.between(validSegments.get(validSegments.size() - 1).getArrivalTime(), nextSegment.getDepartureTime());
                                                                                if (segmentWaiting >= 0 && segmentWaiting <= MAX_WAITING_TIME_MINUTES && !nextSegment.getDepartureTime().isAfter(maxTime)) {
                                                                                        validSegments.add(nextSegment);
                                                                                        if (nextSegment.getArrivalCity().equals(arrivalCity)) break;
                                                                                } else {
                                                                                        break;
                                                                                }
                                                                        }
                                                                        mergedRoute = mergeTripRoutes(validSegments);
                                                                }
                                                        }

                                                        newPath.add(mergedRoute);

                                                        int totalPrice = calculateTotalPrice(newPath);
                                                        long totalDuration = calculateTotalDuration(newPath);
                                                        if (totalDuration > MAX_TOTAL_DURATION_MINUTES) continue;

                                                        RouteWithMetrics newMetrics = new RouteWithMetrics(newPath, totalPrice, totalDuration, newPath.size() - 1);
                                                        String nextCity = mergedRoute.getArrivalCity();

                                                        if (nextCity.equals(arrivalCity)) {
                                                                if (uniquePaths.add(newMetrics) && (bestPaths.size() < MAX_TOTAL_PATHS || comparator.compare(newMetrics, bestPaths.peek()) < 0)) {
                                                                        bestPaths.add(newMetrics);
                                                                        if (bestPaths.size() > MAX_TOTAL_PATHS) bestPaths.poll();
                                                                        improved = true;
                                                                        System.out.println("Added path to " + arrivalCity + ": " + newPath);
                                                                }
                                                        } else {
                                                                newPathsByCity.computeIfAbsent(nextCity, k -> new ArrayList<>()).add(newMetrics);
                                                                improved = true;
                                                        }
                                                }
                                        }
                                }

                                newPathsByCity.forEach((city, paths) -> {
                                        paths.sort(comparator);
                                        visitedPathsByCity.put(city, new HashSet<>(paths.stream().limit(MAX_TOTAL_PATHS).collect(Collectors.toList())));
                                });

                                if (!improved || bestPaths.size() >= MAX_TOTAL_PATHS) break;
                        }
                }

                List<List<RouteDto>> result = new ArrayList<>();
                while (!bestPaths.isEmpty()) {
                        result.add(0, bestPaths.poll().getPath().stream().map(routeMapper::toDto).collect(Collectors.toList()));
                }

                System.out.println("Result: " + result);

                String cacheKey = "routesSearch::" + departureCity + "," + arrivalCity + "," + startTime + "," + directOnly + "," + multiStop + "," +
                        (sort != null ? sort.name() : "DEFAULT") + "," + (transportType != null ? transportType : "null");
                CacheUtils.updateRouteIndex(result, redisTemplate, objectMapper, cacheKey);

                return result;
        }

        private List<Route> findDirectRoutesAndTrips(String departureCity, String arrivalCity, LocalDateTime startTime,
                                                     LocalDateTime maxDepartureTime, String transportType, boolean directOnly) {
                List<Route> relevantRoutes = routeRepository.findRoutesAndTrips(departureCity, arrivalCity, startTime, maxDepartureTime, transportType);
                List<Route> directRoutes = new ArrayList<>();

                if (directOnly) {
                        for (Route route : relevantRoutes) {
                                if (route.getDepartureCity().equals(departureCity) &&
                                        route.getArrivalCity().equals(arrivalCity) &&
                                        !route.getDepartureTime().isBefore(startTime) &&
                                        !route.getDepartureTime().isAfter(maxDepartureTime)) {
                                        directRoutes.add(route);
                                }
                        }

                        Map<Long, List<Route>> routesByTrip = groupRoutesByTrip(relevantRoutes.stream()
                                .collect(Collectors.groupingBy(Route::getDepartureCity)));
                        for (Long trip : routesByTrip.keySet()) {
                                List<Route> tripSegments = routesByTrip.get(trip);
                                tripSegments.sort(Comparator.comparing(Route::getDepartureTime));

                                int startIndex = -1;
                                int endIndex = -1;
                                for (int i = 0; i < tripSegments.size(); i++) {
                                        if (tripSegments.get(i).getDepartureCity().equals(departureCity)) {
                                                startIndex = i;
                                        }
                                        if (tripSegments.get(i).getArrivalCity().equals(arrivalCity)) {
                                                endIndex = i;
                                        }
                                }

                                if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
                                        if (!tripSegments.get(startIndex).getDepartureTime().isBefore(startTime) &&
                                                !tripSegments.get(endIndex).getDepartureTime().isAfter(maxDepartureTime)) {
                                                List<Route> validSegments = new ArrayList<>();
                                                for (int i = startIndex; i <= endIndex; i++) {
                                                        validSegments.add(tripSegments.get(i));
                                                }
                                                Route mergedRoute = mergeTripRoutes(validSegments);
                                                directRoutes.add(mergedRoute);
                                        }
                                }
                        }
                } else {
                        Map<Long, List<Route>> routesByTrip = groupRoutesByTrip(relevantRoutes.stream()
                                .collect(Collectors.groupingBy(Route::getDepartureCity)));
                        for (Long trip : routesByTrip.keySet()) {
                                List<Route> tripSegments = routesByTrip.get(trip);
                                tripSegments.sort(Comparator.comparing(Route::getDepartureTime));

                                int startIndex = -1;
                                for (int i = 0; i < tripSegments.size(); i++) {
                                        if (tripSegments.get(i).getDepartureCity().equals(departureCity)) {
                                                startIndex = i;
                                                break;
                                        }
                                }

                                if (startIndex != -1) {
                                        if (tripSegments.get(startIndex).getDepartureTime().isBefore(startTime) ||
                                                tripSegments.get(startIndex).getDepartureTime().isAfter(maxDepartureTime)) {
                                                continue;
                                        }

                                        for (int i = startIndex; i < tripSegments.size(); i++) {
                                                List<Route> validSegments = new ArrayList<>();
                                                validSegments.add(tripSegments.get(startIndex));
                                                for (int j = startIndex + 1; j <= i; j++) {
                                                        Route nextSegment = tripSegments.get(j);
                                                        long segmentWaiting = ChronoUnit.MINUTES.between(validSegments.get(validSegments.size() - 1).getArrivalTime(), nextSegment.getDepartureTime());
                                                        if (segmentWaiting >= 0 && segmentWaiting <= MAX_WAITING_TIME_MINUTES && !nextSegment.getDepartureTime().isAfter(maxDepartureTime)) {
                                                                validSegments.add(nextSegment);
                                                        } else {
                                                                break;
                                                        }
                                                }

                                                if (!validSegments.isEmpty()) {
                                                        Route mergedRoute = mergeTripRoutes(validSegments);
                                                        directRoutes.add(mergedRoute);
                                                }
                                        }
                                }
                        }
                }

                return directRoutes;
        }

        private String resolveTransportType(String transportType) {
                if (transportType == null || "mix".equalsIgnoreCase(transportType)) return null;
                return transportType.toLowerCase();
        }

        private Map<String, List<Route>> indexRoutes(LocalDateTime minDepartureTime, LocalDateTime maxDepartureTime,
                                                     String arrivalCity, String transportType,
                                                     String departureCity, LocalDateTime maxDepartureTimeForStartCity) {
                Map<String, List<Route>> routesByDepartureCity = new HashMap<>();
                try (Stream<Route> routeStream = routeRepository.streamRoutesInTimeRange(minDepartureTime, maxDepartureTime,
                        arrivalCity, transportType, departureCity, maxDepartureTimeForStartCity)) {
                        routeStream.forEach(route ->
                                routesByDepartureCity.computeIfAbsent(route.getDepartureCity(), k -> new ArrayList<>()).add(route));
                }
                return routesByDepartureCity;
        }

        private Map<Long, List<Route>> groupRoutesByTrip(Map<String, List<Route>> routesByDepartureCity) {
                Map<Long, List<Route>> routesByTrip = new HashMap<>();
                routesByDepartureCity.values().forEach(routes ->
                        routes.forEach(route ->
                                routesByTrip.computeIfAbsent(route.getTrip(), k -> new ArrayList<>()).add(route)));
                return routesByTrip;
        }

        private Route mergeTripRoutes(List<Route> tripRoutes) {
                if (tripRoutes.size() == 1) return tripRoutes.get(0);
                Route first = tripRoutes.get(0);
                Route last = tripRoutes.get(tripRoutes.size() - 1);
                List<Long> routeIds = tripRoutes.stream().map(Route::getId).collect(Collectors.toList());
                return new Route(
                        first.getTrip(),
                        first.getDepartureCity(),
                        last.getArrivalCity(),
                        first.getDepartureTime(),
                        last.getArrivalTime(),
                        Duration.between(first.getDepartureTime(), last.getArrivalTime()),
                        tripRoutes.stream().mapToInt(Route::getMinPrice).sum(),
                        first.getTransportType(),
                        tripRoutes.stream().allMatch(Route::isHasAvailableTickets),
                        first.getTrip(),
                        routeIds
                );
        }

        private boolean isValidTransfer(RouteWithMetrics prevPath, Route nextRoute, long waitingTime) {
                if (prevPath.getPath().isEmpty()) return true;
                Route lastRoute = prevPath.getPath().get(prevPath.getPath().size() - 1);
                if (lastRoute.getTrip().equals(nextRoute.getTrip())) {
                        return waitingTime >= 0 && waitingTime <= MAX_WAITING_TIME_MINUTES;
                }
                long minWaitingTime = lastRoute.getTransportType().equals(nextRoute.getTransportType()) ? 60 : 180;
                return waitingTime >= minWaitingTime && waitingTime <= MAX_WAITING_TIME_MINUTES;
        }

        private int calculateTotalPrice(List<Route> path) {
                return path.stream().mapToInt(Route::getMinPrice).sum();
        }

        private long calculateTotalDuration(List<Route> path) {
                if (path.isEmpty()) return 0;
                LocalDateTime start = path.get(0).getDepartureTime();
                LocalDateTime end = path.get(path.size() - 1).getArrivalTime();
                return ChronoUnit.MINUTES.between(start, end);
        }

        private Comparator<RouteWithMetrics> getComparator(SortCriteria sort) {
                return (p1, p2) -> {
                        if (p1.getPath().isEmpty() && p2.getPath().isEmpty()) return 0;
                        if (p1.getPath().isEmpty()) return 1;
                        if (p2.getPath().isEmpty()) return -1;

                        switch (sort != null ? sort : SortCriteria.DEFAULT) {
                                case CHEAPEST:
                                        return Integer.compare(p2.getTotalPrice(), p1.getTotalPrice());
                                case EXPENSIVE:
                                        return Integer.compare(p1.getTotalPrice(), p2.getTotalPrice());
                                case DURATION:
                                        return Long.compare(p2.getTotalDuration(), p1.getTotalDuration());
                                case AVAILABILITY:
                                        boolean avail1 = p1.hasAvailableTickets();
                                        boolean avail2 = p2.hasAvailableTickets();
                                        if (avail1 && !avail2) return 1;
                                        if (!avail1 && avail2) return -1;
                                        return p2.getDepartureTime().compareTo(p1.getDepartureTime());
                                case DEFAULT:
                                        int transfersCompare = Integer.compare(p2.getTransfers(), p1.getTransfers());
                                        if (transfersCompare != 0) return transfersCompare;
                                        return p2.getDepartureTime().compareTo(p1.getDepartureTime());
                                default:
                                        return p2.getDepartureTime().compareTo(p1.getDepartureTime());
                        }
                };
        }

        @Cacheable(value = "routesDirect", key = "{#departureCity, #arrivalCity, #transportType, #sortCriteria}")
        public Map<LocalDate, List<RouteDto>> getDirectRoutesGroupedByDate(String departureCity, String arrivalCity,
                                                                           String transportType, SortCriteria sortCriteria) {
                LocalDateTime startDate = LocalDateTime.now();
                LocalDateTime endDate = startDate.plusDays(14);
                String sortCriteriaStr = sortCriteria != null ? sortCriteria.name() : SortCriteria.DEFAULT.name();
                String effectiveTransportType = (transportType == null || transportType.trim().isEmpty()) ? "mix" : transportType.toLowerCase();

                List<RouteDto> routes = routeRepository.findDirectRoutesWithSort(departureCity, arrivalCity, startDate, endDate,
                                effectiveTransportType.equals("mix") ? null : effectiveTransportType, sortCriteriaStr)
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