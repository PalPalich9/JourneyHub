package com.example.JourneyHub.service.route;

import com.example.JourneyHub.model.entity.Route;
import com.example.JourneyHub.model.entity.RouteWithMetrics;
import com.example.JourneyHub.model.enums.SortCriteria;
import com.example.JourneyHub.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class RouteFinder {

    private final RouteRepository routeRepository;

    private static final int MAX_TOTAL_PATHS = 50;
    private static final long MAX_TOTAL_DURATION_MINUTES = 96 * 60;
    private static final long MAX_WAITING_TIME_MINUTES = 25 * 60;
    private static final long MIN_SAME_TRANSPORT_WAITING_MINUTES = 60;
    private static final long MIN_DIFF_TRANSPORT_WAITING_MINUTES = 180;

     @Transactional(readOnly = true)
     public List<List<Route>> findRoutes(String departureCity, String arrivalCity,
                                         LocalDateTime startTime, SortCriteria sort,
                                         String transportType, boolean directOnly, boolean multiStop,
                                         LocalDateTime maxDepartureTimeForStartCity, LocalDateTime maxDepartureTime,
                                         boolean restrictPathLimit) {
         int maxTransfers = directOnly ? 0 : (multiStop ? 3 : 1);

         String resolvedTransportType = resolveTransportType(transportType);

         List<Route> allRoutes;
         try (Stream<Route> routeStream = routeRepository.streamRoutesInTimeRange(
                 startTime, maxDepartureTime, arrivalCity, resolvedTransportType, departureCity, maxDepartureTimeForStartCity)) {
             allRoutes = routeStream.collect(Collectors.toList());
         }

         Map<Long, List<Route>> routesByTrip = groupRoutesByTrip(allRoutes);

         Comparator<RouteWithMetrics> comparator = getComparator(sort);

         Collection<RouteWithMetrics> bestPaths = restrictPathLimit
                 ? new PriorityQueue<>(MAX_TOTAL_PATHS + 1, comparator)
                 : new ArrayList<>();

         Set<String> uniquePathSignatures = new HashSet<>();

         processDirectRoutes(departureCity, arrivalCity, startTime, maxDepartureTimeForStartCity, routesByTrip, bestPaths, uniquePathSignatures, directOnly);

         if (!directOnly) {
             Map<String, Set<RouteWithMetrics>> visitedPathsByCity = new HashMap<>();
             visitedPathsByCity.put(departureCity, new HashSet<>(Collections.singletonList(new RouteWithMetrics(new ArrayList<>(), 0, 0, 0))));

             processInitialTripSegments(departureCity, startTime, maxDepartureTimeForStartCity, routesByTrip, visitedPathsByCity);

             Map<String, List<Route>> routesByDepartureCity = indexRoutes(allRoutes);

             for (int round = 0; round < maxTransfers && (restrictPathLimit ? bestPaths.size() < MAX_TOTAL_PATHS : true); round++) {
                 Map<String, List<RouteWithMetrics>> newPathsByCity = new HashMap<>();
                 boolean improved = true;
                 int extraChecks = 0;
                 int maxExtraChecks = 0;

                 for (String currentCity : new ArrayList<>(visitedPathsByCity.keySet())) {
                     Set<RouteWithMetrics> currentPaths = visitedPathsByCity.get(currentCity);
                     List<Route> possibleRoutes = routesByDepartureCity.getOrDefault(currentCity, Collections.emptyList());

                     for (Route route : possibleRoutes) {
                         LocalDateTime maxTime = currentCity.equals(departureCity) ? maxDepartureTimeForStartCity : maxDepartureTime;
                         if (route.getDepartureTime().isBefore(startTime) || route.getDepartureTime().isAfter(maxTime)) {
                             continue;
                         }

                         for (RouteWithMetrics prevPath : currentPaths) {
                             long waitingTime = prevPath.getPath().isEmpty()
                                     ? 0
                                     : ChronoUnit.MINUTES.between(prevPath.getPath().get(prevPath.getPath().size() - 1).getArrivalTime(), route.getDepartureTime());

                             if (!prevPath.getPath().isEmpty() && prevPath.getPath().get(prevPath.getPath().size() - 1).getTrip().equals(route.getTrip())) {
                                 continue;
                             }

                             if (!isValidTransfer(prevPath, route, waitingTime)) {
                                 continue;
                             }

                             List<Route> mergedRoutes = mergeWithTrip(route, routesByTrip, maxTime, arrivalCity);
                             for (Route mergedRoute : mergedRoutes) {
                                 List<Route> newPath = new ArrayList<>(prevPath.getPath());
                                 newPath.add(mergedRoute);

                                 int totalPrice = calculateTotalPrice(newPath);
                                 long totalDuration = calculateTotalDuration(newPath);
                                 if (totalDuration > MAX_TOTAL_DURATION_MINUTES) {
                                     continue;
                                 }

                                 RouteWithMetrics newMetrics = new RouteWithMetrics(newPath, totalPrice, totalDuration, newPath.size() - 1);
                                 String pathSignature = generatePathSignature(newPath);
                                 String nextCity = mergedRoute.getArrivalCity();

                                 if (nextCity.equals(arrivalCity)) {
                                     if (uniquePathSignatures.add(pathSignature)) {
                                         if (restrictPathLimit) {
                                             PriorityQueue<RouteWithMetrics> queue = (PriorityQueue<RouteWithMetrics>) bestPaths;
                                             if (queue.size() < MAX_TOTAL_PATHS) {
                                                 queue.offer(newMetrics);
                                                 improved = true;
                                             } else if (comparator.compare(newMetrics, queue.peek()) < 0) {
                                                 queue.poll();
                                                 queue.offer(newMetrics);


                                                 List<RouteWithMetrics> sortedResults = new ArrayList<>(queue);
                                                 sortedResults.sort(comparator);
                                                 int rank = sortedResults.indexOf(newMetrics);

                                                 if (rank < 10) {
                                                     maxExtraChecks = 10;
                                                     extraChecks = Math.max(extraChecks, maxExtraChecks);
                                                     improved = true;
                                                 } else if (rank < 20) {
                                                     maxExtraChecks = 5;
                                                     extraChecks = Math.max(extraChecks, maxExtraChecks);
                                                     improved = true;
                                                 } else {
                                                     maxExtraChecks = 1;
                                                     extraChecks = Math.max(extraChecks, maxExtraChecks);
                                                     improved = true;
                                                 }
                                             } else {
                                                 if (extraChecks > 0) {
                                                     extraChecks--;
                                                     improved = true;
                                                 } else {
                                                     improved = false;
                                                 }
                                             }
                                         } else {
                                             bestPaths.add(newMetrics);
                                             improved = true;
                                         }
                                     }
                                 } else {
                                     newPathsByCity.computeIfAbsent(nextCity, k -> new ArrayList<>()).add(newMetrics);
                                     improved = true;
                                 }
                             }
                         }

                         if (restrictPathLimit && bestPaths.size() >= MAX_TOTAL_PATHS && !improved && extraChecks == 0) {
                             break;
                         }
                     }

                     if (restrictPathLimit && bestPaths.size() >= MAX_TOTAL_PATHS && !improved && extraChecks == 0) {
                         break;
                     }
                 }

                 newPathsByCity.forEach((city, paths) -> {
                     paths.sort(comparator);
                     visitedPathsByCity.computeIfAbsent(city, k -> new HashSet<>())
                             .addAll(paths.stream().limit(restrictPathLimit ? MAX_TOTAL_PATHS : Integer.MAX_VALUE).collect(Collectors.toList()));
                 });

                 if (!improved && (restrictPathLimit && bestPaths.size() >= MAX_TOTAL_PATHS && extraChecks == 0)) {
                     break;
                 }
             }
         }

         List<List<Route>> result;
         if (restrictPathLimit) {
             result = new ArrayList<>();
             PriorityQueue<RouteWithMetrics> queue = (PriorityQueue<RouteWithMetrics>) bestPaths;
             while (!queue.isEmpty()) {
                 result.add(0, queue.poll().getPath());
             }
         } else {
             result = bestPaths.stream()
                     .sorted(comparator)
                     .map(RouteWithMetrics::getPath)
                     .collect(Collectors.toList());
         }
         return result;
     }
    public List<List<Route>> findRoutes(String departureCity, String arrivalCity,
                                        LocalDateTime startTime, SortCriteria sort,
                                        String transportType, boolean directOnly, boolean multiStop) {
        LocalDateTime maxDepartureTimeForStartCity = startTime.toLocalDate().atTime(23, 59, 59);
        LocalDateTime maxDepartureTime = startTime.toLocalDate().plusDays(2).atTime(23, 59, 59);
        return findRoutes(departureCity, arrivalCity, startTime, sort, transportType, directOnly, multiStop,
                maxDepartureTimeForStartCity, maxDepartureTime, true);
    }
    private void processDirectRoutes(String departureCity, String arrivalCity, LocalDateTime startTime,
                                     LocalDateTime maxDepartureTime, Map<Long, List<Route>> routesByTrip,
                                     Collection<RouteWithMetrics> bestPaths, Set<String> uniquePathSignatures, boolean directOnly) {
        for (Long trip : routesByTrip.keySet()) {
            List<Route> tripSegments = routesByTrip.get(trip);
            tripSegments.sort(Comparator.comparing(Route::getDepartureTime));

            int startIndex = -1;
            int endIndex = -1;
            for (int i = 0; i < tripSegments.size(); i++) {
                if (tripSegments.get(i).getDepartureCity().equals(departureCity)) startIndex = i;
                if (tripSegments.get(i).getArrivalCity().equals(arrivalCity)) endIndex = i;
            }

            if (startIndex != -1 && (!directOnly || (endIndex != -1 && startIndex <= endIndex))) {
                List<Route> validSegments = new ArrayList<>();
                if (endIndex == -1 || endIndex < startIndex) endIndex = tripSegments.size() - 1;

                Route firstSegment = tripSegments.get(startIndex);
                if (firstSegment.getDepartureTime().isBefore(startTime) || firstSegment.getDepartureTime().isAfter(maxDepartureTime)) {
                    continue;
                }

                for (int i = startIndex; i <= endIndex; i++) {
                    Route segment = tripSegments.get(i);
                    validSegments.add(segment);
                    if (segment.getArrivalCity().equals(arrivalCity)) break;
                }

                if (!validSegments.isEmpty() && validSegments.get(validSegments.size() - 1).getArrivalCity().equals(arrivalCity)) {
                    Route mergedRoute = mergeTripRoutes(validSegments);
                    List<Route> path = new ArrayList<>();
                    path.add(mergedRoute);
                    String pathSignature = generatePathSignature(path);
                    RouteWithMetrics metrics = new RouteWithMetrics(path, mergedRoute.getMinPrice(),
                            calculateTotalDuration(path), 0);
                    if (uniquePathSignatures.add(pathSignature)) {
                        bestPaths.add(metrics);

                    }
                }
            }
        }
    }

    private void processInitialTripSegments(String departureCity, LocalDateTime startTime,
                                            LocalDateTime maxDepartureTime, Map<Long, List<Route>> routesByTrip,
                                            Map<String, Set<RouteWithMetrics>> visitedPathsByCity) {
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

            if (startIndex != -1 && !tripSegments.get(startIndex).getDepartureTime().isBefore(startTime)
                    && !tripSegments.get(startIndex).getDepartureTime().isAfter(maxDepartureTime)) {
                List<Route> validSegments = new ArrayList<>();
                for (int i = startIndex; i < tripSegments.size(); i++) {
                    Route segment = tripSegments.get(i);
                    if (segment.getDepartureTime().isAfter(maxDepartureTime)) {
                        break;
                    }
                    validSegments.add(segment);
                    Route mergedRoute = mergeTripRoutes(validSegments);
                    List<Route> path = new ArrayList<>();
                    path.add(mergedRoute);
                    RouteWithMetrics metrics = new RouteWithMetrics(path, mergedRoute.getMinPrice(),
                            calculateTotalDuration(path), 0);
                    visitedPathsByCity.computeIfAbsent(mergedRoute.getArrivalCity(), k -> new HashSet<>()).add(metrics);
                }
            }
        }
    }

    private List<Route> mergeWithTrip(Route route, Map<Long, List<Route>> routesByTrip, LocalDateTime maxTime, String arrivalCity) {
        List<Route> tripSegments = routesByTrip.getOrDefault(route.getTrip(), Collections.singletonList(route));
        tripSegments.sort(Comparator.comparing(Route::getDepartureTime));
        int routeIndex = tripSegments.indexOf(route);

        if (routeIndex == -1) return Collections.singletonList(route);

        List<Route> mergedRoutes = new ArrayList<>();
        List<Route> validSegments = new ArrayList<>();
        validSegments.add(route);
        mergedRoutes.add(mergeTripRoutes(validSegments));

        for (int i = routeIndex + 1; i < tripSegments.size(); i++) {
            Route nextSegment = tripSegments.get(i);
            long waiting = ChronoUnit.MINUTES.between(validSegments.get(validSegments.size() - 1).getArrivalTime(), nextSegment.getDepartureTime());
            if (waiting >= 0 && waiting <= MAX_WAITING_TIME_MINUTES && !nextSegment.getDepartureTime().isAfter(maxTime)) {
                validSegments.add(nextSegment);
                Route merged = mergeTripRoutes(validSegments);
                mergedRoutes.add(merged);
                if (nextSegment.getArrivalCity().equals(arrivalCity)) break;
            } else {
                break;
            }
        }
        return mergedRoutes;
    }

    private String generatePathSignature(List<Route> path) {
        return path.stream()
                .map(r -> r.getDepartureCity() + "->" + r.getArrivalCity() + "(trip=" + r.getTrip() + ")")
                .collect(Collectors.joining(","));
    }

    private String resolveTransportType(String transportType) {
        if (transportType == null || "mix".equalsIgnoreCase(transportType)) return null;
        return transportType.toLowerCase();
    }

    private Map<String, List<Route>> indexRoutes(List<Route> allRoutes) {
        return allRoutes.stream().collect(Collectors.groupingBy(Route::getDepartureCity));
    }

    private Map<Long, List<Route>> groupRoutesByTrip(List<Route> routes) {
        return routes.stream().collect(Collectors.groupingBy(Route::getTrip));
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
                java.time.Duration.between(first.getDepartureTime(), last.getArrivalTime()),
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
        long minWaitingTime = lastRoute.getTransportType().equals(nextRoute.getTransportType())
                ? MIN_SAME_TRANSPORT_WAITING_MINUTES
                : MIN_DIFF_TRANSPORT_WAITING_MINUTES;
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
    @Transactional(readOnly = true)
    public Map<LocalDate, List<Route>> findDirectRoutesGroupedByDate(String departureCity, String arrivalCity,
                                                                     String transportType, SortCriteria sortCriteria) {
        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusDays(14);

        LocalDateTime maxDepartureTimeForStartCity = endTime;

        List<List<Route>> routes = findRoutes(departureCity, arrivalCity, startTime, sortCriteria, transportType, true, false,
                maxDepartureTimeForStartCity, endTime, false);

        Comparator<RouteWithMetrics> comparator = getComparator(sortCriteria).reversed();

        return routes.stream()
                .filter(path -> !path.isEmpty())
                .map(path -> new RouteWithMetrics(path, calculateTotalPrice(path), calculateTotalDuration(path), path.size() - 1))
                .collect(Collectors.groupingBy(
                        metrics -> metrics.getDepartureTime().toLocalDate(),
                        TreeMap::new,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                metricsList -> metricsList.stream()
                                        .sorted(comparator)
                                        .map(RouteWithMetrics::getPath)
                                        .map(this::mergeTripRoutes)
                                        .collect(Collectors.toList())
                        )
                ));
    }
}