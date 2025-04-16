package com.example.JourneyHub.service.route;

import com.example.JourneyHub.model.entity.Route;
import com.example.JourneyHub.model.entity.RouteWithMetrics;
import com.example.JourneyHub.model.enums.SortCriteria;
import com.example.JourneyHub.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class RouteFinderTest {

    @Mock
    private RouteRepository routeRepository;

    @InjectMocks
    private RouteFinder routeFinder;

    private List<Route> sampleRoutes;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleRoutes = createSampleRoutes();
    }

    private List<Route> createSampleRoutes() {
        List<Route> routes = new ArrayList<>();
        routes.add(new Route(9424300L, "Урал", "Канск", LocalDateTime.parse("2025-05-08T12:00:00"),
                LocalDateTime.parse("2025-05-08T14:00:00"), Duration.ofHours(2), 2000, "bus", true, 777L));
        routes.add(new Route(9424301L, "Канск", "Братск", LocalDateTime.parse("2025-05-08T14:30:00"),
                LocalDateTime.parse("2025-05-08T16:30:00"), Duration.ofHours(2), 2000, "bus", true, 777L));
        routes.add(new Route(9424296L, "Братск", "Кызыл", LocalDateTime.parse("2025-05-09T00:00:00"),
                LocalDateTime.parse("2025-05-09T03:00:00"), Duration.ofHours(3), 2500, "train", false, 888L));
        routes.add(new Route(9424297L, "Кызыл", "Абакан", LocalDateTime.parse("2025-05-09T03:30:00"),
                LocalDateTime.parse("2025-05-09T06:30:00"), Duration.ofHours(3), 2500, "train", true, 888L));
        routes.add(new Route(9424298L, "Абакан", "Красноярск", LocalDateTime.parse("2025-05-09T07:00:00"),
                LocalDateTime.parse("2025-05-09T10:30:00"), Duration.ofHours(3).plusMinutes(30), 2500, "train", true, 888L));
        routes.add(new Route(9424299L, "Красноярск", "Бородино", LocalDateTime.parse("2025-05-09T11:00:00"),
                LocalDateTime.parse("2025-05-09T13:00:00"), Duration.ofHours(2), 2500, "train", true, 888L));
        routes.add(new Route(8885190L, "Красноярск", "Москва", LocalDateTime.parse("2025-05-09T12:00:00"),
                LocalDateTime.parse("2025-05-09T19:00:00"), Duration.ofHours(7), 5000, "train", true, 8885190L));
        routes.add(new Route(8886281L, "Москва", "Воронеж", LocalDateTime.parse("2025-05-10T00:00:00"),
                LocalDateTime.parse("2025-05-10T05:00:00"), Duration.ofHours(5), 2500, "train", true, 8886281L));
        return routes;
    }

    @Test
    public void testFindRoutesDirectOnly() {
        when(routeRepository.streamRoutesInTimeRange(any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleRoutes.stream());

        List<List<Route>> result = routeFinder.findRoutes("Урал", "Братск",
                LocalDateTime.parse("2025-05-08T11:00:00"), SortCriteria.DURATION, null, true, false);

        assertEquals(1, result.size());
        List<Route> path = result.get(0);
        assertEquals(1, path.size());
        assertEquals("Урал", path.get(0).getDepartureCity());
        assertEquals("Братск", path.get(0).getArrivalCity());
        assertEquals(777L, path.get(0).getTrip());
        assertEquals(2, path.get(0).getRouteIds().size());
    }

    @Test
    public void testFindRoutesWithMultipleStops() {
        when(routeRepository.streamRoutesInTimeRange(any(), any(), any(), any(), any(), any()))
                .thenReturn(sampleRoutes.stream());

        List<List<Route>> result = routeFinder.findRoutes("Урал", "Москва",
                LocalDateTime.parse("2025-05-08T11:00:00"), SortCriteria.DURATION, null, false, true);

        assertEquals(1, result.size());
        List<Route> path = result.get(0);
        assertEquals(3, path.size());
        assertEquals("Урал", path.get(0).getDepartureCity());
        assertEquals("Братск", path.get(0).getArrivalCity());
        assertEquals("Братск", path.get(1).getDepartureCity());
        assertEquals("Красноярск", path.get(1).getArrivalCity());
        assertEquals("Красноярск", path.get(2).getDepartureCity());
        assertEquals("Москва", path.get(2).getArrivalCity());
    }

    @Test
    public void testProcessDirectRoutes() throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        Map<Long, List<Route>> routesByTrip = sampleRoutes.stream().collect(Collectors.groupingBy(Route::getTrip));

        java.lang.reflect.Method getComparatorMethod = RouteFinder.class.getDeclaredMethod("getComparator", SortCriteria.class);
        getComparatorMethod.setAccessible(true);
        @SuppressWarnings("unchecked")
        Comparator<RouteWithMetrics> comparator = (Comparator<RouteWithMetrics>) getComparatorMethod.invoke(routeFinder, SortCriteria.DURATION);

        Collection<RouteWithMetrics> bestPaths = new ArrayList<>();
        Set<String> uniquePathSignatures = new HashSet<>();

        java.lang.reflect.Method method = RouteFinder.class.getDeclaredMethod("processDirectRoutes",
                String.class, String.class, LocalDateTime.class, LocalDateTime.class, Map.class, Collection.class, Set.class, boolean.class);
        method.setAccessible(true);
        method.invoke(routeFinder, "Урал", "Братск", LocalDateTime.parse("2025-05-08T11:00:00"),
                LocalDateTime.parse("2025-05-08T23:59:59"), routesByTrip, bestPaths, uniquePathSignatures, true);

        assertEquals(1, bestPaths.size());
        RouteWithMetrics path = bestPaths.iterator().next();
        assertEquals("Урал", path.getPath().get(0).getDepartureCity());
        assertEquals("Братск", path.getPath().get(0).getArrivalCity());
    }

    @Test
    public void testProcessInitialTripSegments() throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        Map<Long, List<Route>> routesByTrip = sampleRoutes.stream().collect(Collectors.groupingBy(Route::getTrip));
        Map<String, Set<RouteWithMetrics>> visitedPathsByCity = new HashMap<>();

        java.lang.reflect.Method method = RouteFinder.class.getDeclaredMethod("processInitialTripSegments",
                String.class, LocalDateTime.class, LocalDateTime.class, Map.class, Map.class);
        method.setAccessible(true);
        method.invoke(routeFinder, "Урал", LocalDateTime.parse("2025-05-08T11:00:00"),
                LocalDateTime.parse("2025-05-08T23:59:59"), routesByTrip, visitedPathsByCity);

        assertTrue(visitedPathsByCity.containsKey("Канск"));
        assertTrue(visitedPathsByCity.containsKey("Братск"));
        assertEquals(1, visitedPathsByCity.get("Братск").size());
        RouteWithMetrics path = visitedPathsByCity.get("Братск").iterator().next();
        assertEquals("Урал", path.getPath().get(0).getDepartureCity());
        assertEquals("Братск", path.getPath().get(0).getArrivalCity());
    }

    @Test
    public void testMergeWithTrip() throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        Map<Long, List<Route>> routesByTrip = sampleRoutes.stream().collect(Collectors.groupingBy(Route::getTrip));
        Route startRoute = sampleRoutes.stream().filter(r -> r.getId().equals(9424296L)).findFirst().get();

        java.lang.reflect.Method method = RouteFinder.class.getDeclaredMethod("mergeWithTrip",
                Route.class, Map.class, LocalDateTime.class, String.class);
        method.setAccessible(true);
        List<Route> mergedRoutes = (List<Route>) method.invoke(routeFinder, startRoute, routesByTrip,
                LocalDateTime.parse("2025-05-10T23:59:59"), "Москва");

        assertEquals(4, mergedRoutes.size());
        assertEquals("Братск", mergedRoutes.get(0).getDepartureCity());
        assertEquals("Кызыл", mergedRoutes.get(0).getArrivalCity());
        assertEquals("Братск", mergedRoutes.get(1).getDepartureCity());
        assertEquals("Абакан", mergedRoutes.get(1).getArrivalCity());
        assertEquals("Братск", mergedRoutes.get(2).getDepartureCity());
        assertEquals("Красноярск", mergedRoutes.get(2).getArrivalCity());
        assertEquals("Братск", mergedRoutes.get(3).getDepartureCity());
        assertEquals("Бородино", mergedRoutes.get(3).getArrivalCity());
    }

    @Test
    public void testGeneratePathSignature() throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        List<Route> path = Arrays.asList(
                sampleRoutes.get(0), // Урал -> Канск
                sampleRoutes.get(1)  // Канск -> Братск
        );

        java.lang.reflect.Method method = RouteFinder.class.getDeclaredMethod("generatePathSignature", List.class);
        method.setAccessible(true);
        String signature = (String) method.invoke(routeFinder, path);

        assertEquals("Урал->Канск(trip=777),Канск->Братск(trip=777)", signature);
    }

    @Test
    public void testResolveTransportType() throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        java.lang.reflect.Method method = RouteFinder.class.getDeclaredMethod("resolveTransportType", String.class);
        method.setAccessible(true);

        assertNull(method.invoke(routeFinder, (String) null));
        assertNull(method.invoke(routeFinder, "mix"));
        assertEquals("train", method.invoke(routeFinder, "TRAIN"));
    }

    @Test
    public void testIndexRoutes() throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        java.lang.reflect.Method method = RouteFinder.class.getDeclaredMethod("indexRoutes", List.class);
        method.setAccessible(true);
        Map<String, List<Route>> indexed = (Map<String, List<Route>>) method.invoke(routeFinder, sampleRoutes);

        assertEquals(7, indexed.size());
        assertTrue(indexed.containsKey("Урал"));
        assertTrue(indexed.containsKey("Красноярск"));
        assertEquals(2, indexed.get("Красноярск").size());
    }

    @Test
    public void testGroupRoutesByTrip() throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        java.lang.reflect.Method method = RouteFinder.class.getDeclaredMethod("groupRoutesByTrip", List.class);
        method.setAccessible(true);
        Map<Long, List<Route>> grouped = (Map<Long, List<Route>>) method.invoke(routeFinder, sampleRoutes);

        assertEquals(4, grouped.size());
        assertEquals(2, grouped.get(777L).size());
        assertEquals(4, grouped.get(888L).size());
    }

    @Test
    public void testMergeTripRoutes() throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        List<Route> tripRoutes = sampleRoutes.stream().filter(r -> r.getTrip().equals(777L)).collect(Collectors.toList());

        java.lang.reflect.Method method = RouteFinder.class.getDeclaredMethod("mergeTripRoutes", List.class);
        method.setAccessible(true);
        Route merged = (Route) method.invoke(routeFinder, tripRoutes);

        assertEquals("Урал", merged.getDepartureCity());
        assertEquals("Братск", merged.getArrivalCity());
        assertEquals(4000, merged.getMinPrice());
        assertEquals(2, merged.getRouteIds().size());
    }

    @Test
    public void testIsValidTransfer() throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        RouteWithMetrics prevPath = new RouteWithMetrics(Collections.singletonList(sampleRoutes.get(0)), 2000, 120, 0);
        Route nextRoute = sampleRoutes.get(2); // Братск -> Кызыл

        java.lang.reflect.Method method = RouteFinder.class.getDeclaredMethod("isValidTransfer", RouteWithMetrics.class, Route.class, long.class);
        method.setAccessible(true);

        boolean valid = (boolean) method.invoke(routeFinder, prevPath, nextRoute, 450L); // 450 минут ожидания
        assertTrue(valid); // bus -> train, 450 >= 180 и <= 1500

        valid = (boolean) method.invoke(routeFinder, prevPath, nextRoute, 30L); // 30 минут ожидания
        assertFalse(valid); // bus -> train, 30 < 180
    }

    @Test
    public void testCalculateTotalPrice() throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        List<Route> path = Arrays.asList(sampleRoutes.get(0), sampleRoutes.get(1));

        java.lang.reflect.Method method = RouteFinder.class.getDeclaredMethod("calculateTotalPrice", List.class);
        method.setAccessible(true);
        int price = (int) method.invoke(routeFinder, path);

        assertEquals(4000, price);
    }

    @Test
    public void testCalculateTotalDuration() throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        List<Route> path = Arrays.asList(sampleRoutes.get(0), sampleRoutes.get(1));

        java.lang.reflect.Method method = RouteFinder.class.getDeclaredMethod("calculateTotalDuration", List.class);
        method.setAccessible(true);
        long duration = (long) method.invoke(routeFinder, path);

        assertEquals(270, duration); // 12:00 -> 16:30 = 4 часа 30 минут
    }

    @Test
    public void testGetComparator() throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException {
        java.lang.reflect.Method method = RouteFinder.class.getDeclaredMethod("getComparator", SortCriteria.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Comparator<RouteWithMetrics> comparator = (Comparator<RouteWithMetrics>) method.invoke(routeFinder, SortCriteria.DURATION);

        RouteWithMetrics path1 = new RouteWithMetrics(Collections.singletonList(sampleRoutes.get(0)), 2000, 120, 0);
        RouteWithMetrics path2 = new RouteWithMetrics(Arrays.asList(sampleRoutes.get(0), sampleRoutes.get(1)), 4000, 270, 1);


        assertTrue(comparator.compare(path1, path2) > 0, "Path1 (120 min) should be less than Path2 (270 min) for DURATION sort");
    }
}