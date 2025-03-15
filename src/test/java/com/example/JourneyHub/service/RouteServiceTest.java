package com.example.JourneyHub.service;

import com.example.JourneyHub.model.dto.RouteDto;
import com.example.JourneyHub.model.entity.Route;
import com.example.JourneyHub.model.enums.SortCriteria;
import com.example.JourneyHub.model.mapper.RouteMapper;
import com.example.JourneyHub.repository.RouteRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RouteMapper routeMapper;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private RouteService routeService;

    private Route moscowToNovosibirsk;  // id = 8760820
    private Route voronezhToMoscow;     // id = 8762181
    private RouteDto moscowToNovosibirskDto;
    private RouteDto voronezhToMoscowDto;

    @BeforeEach
    void setUp() {
        // Маршрут Москва → Новосибирск (id = 8760820)
        moscowToNovosibirsk = new Route();
        moscowToNovosibirsk.setId(8760820L);
        moscowToNovosibirsk.setDepartureCity("Москва");
        moscowToNovosibirsk.setArrivalCity("Новосибирск");
        moscowToNovosibirsk.setDepartureTime(LocalDateTime.of(2025, 3, 16, 12, 0));
        moscowToNovosibirsk.setArrivalTime(LocalDateTime.of(2025, 3, 16, 15, 0));
        moscowToNovosibirsk.setTransportType("air");
        moscowToNovosibirsk.setTravelDuration(Duration.ofHours(3));
        moscowToNovosibirsk.setHasAvailableTickets(true);
        moscowToNovosibirsk.setMinPrice(10000);

        moscowToNovosibirskDto = new RouteDto();
        moscowToNovosibirskDto.setId(8760820L);
        moscowToNovosibirskDto.setDepartureCity("Москва");
        moscowToNovosibirskDto.setArrivalCity("Новосибирск");
        moscowToNovosibirskDto.setDepartureTime(LocalDateTime.of(2025, 3, 16, 12, 0));
        moscowToNovosibirskDto.setArrivalTime(LocalDateTime.of(2025, 3, 16, 15, 0));
        moscowToNovosibirskDto.setTravelDuration(Duration.ofHours(3));
        moscowToNovosibirskDto.setTransportType("air");
        moscowToNovosibirskDto.setHasAvailableTickets(true);
        moscowToNovosibirskDto.setMinPrice(10000);

        // Маршрут Воронеж → Москва (id = 8762181)
        voronezhToMoscow = new Route();
        voronezhToMoscow.setId(8762181L);
        voronezhToMoscow.setDepartureCity("Воронеж");
        voronezhToMoscow.setArrivalCity("Москва");
        voronezhToMoscow.setDepartureTime(LocalDateTime.of(2025, 3, 16, 0, 0));
        voronezhToMoscow.setArrivalTime(LocalDateTime.of(2025, 3, 16, 2, 0));
        voronezhToMoscow.setTransportType("air");
        voronezhToMoscow.setTravelDuration(Duration.ofHours(2));
        voronezhToMoscow.setHasAvailableTickets(true);
        voronezhToMoscow.setMinPrice(7000);

        voronezhToMoscowDto = new RouteDto();
        voronezhToMoscowDto.setId(8762181L);
        voronezhToMoscowDto.setDepartureCity("Воронеж");
        voronezhToMoscowDto.setArrivalCity("Москва");
        voronezhToMoscowDto.setDepartureTime(LocalDateTime.of(2025, 3, 16, 0, 0));
        voronezhToMoscowDto.setArrivalTime(LocalDateTime.of(2025, 3, 16, 2, 0));
        voronezhToMoscowDto.setTravelDuration(Duration.ofHours(2));
        voronezhToMoscowDto.setTransportType("air");
        voronezhToMoscowDto.setHasAvailableTickets(true);
        voronezhToMoscowDto.setMinPrice(7000);
    }

    @Test
    void testFindDirectRoutes() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2025, 3, 16, 0, 0); // Начало дня маршрута
        String departureCity = "Москва";
        String arrivalCity = "Новосибирск";
        String transportType = "air";

        List<Route> mockRoutes = Collections.singletonList(moscowToNovosibirsk);
        when(routeRepository.findDirectRoutes(any(), any(), eq(departureCity), eq(arrivalCity), eq(transportType)))
                .thenReturn(mockRoutes);
        when(routeMapper.toDto(moscowToNovosibirsk)).thenReturn(moscowToNovosibirskDto);
        when(redisTemplate.opsForSet()).thenReturn(mock(SetOperations.class));

        // Act
        List<List<RouteDto>> result = routeService.findRoutes(
                departureCity, arrivalCity, startTime, SortCriteria.DEFAULT,
                transportType, true, false);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size(), "Ожидается один маршрут");
        assertEquals(1, result.get(0).size(), "Ожидается один сегмент в маршруте");
        assertEquals(moscowToNovosibirskDto, result.get(0).get(0), "Маршрут должен совпадать с ожидаемым DTO");

        verify(routeRepository).findDirectRoutes(any(), any(), eq(departureCity), eq(arrivalCity), eq(transportType));
        verify(routeMapper).toDto(moscowToNovosibirsk);
    }

    @Test
    void testFindRoutesWithSorting() {
        // Arrange
        LocalDateTime startTime = LocalDateTime.of(2025, 3, 16, 0, 0);
        String departureCity = "Москва";
        String arrivalCity = "Новосибирск";

        // Второй маршрут Москва → Новосибирск с более высокой ценой
        Route expensiveRoute = new Route();
        expensiveRoute.setId(8760821L);
        expensiveRoute.setDepartureCity("Москва");
        expensiveRoute.setArrivalCity("Новосибирск");
        expensiveRoute.setDepartureTime(LocalDateTime.of(2025, 3, 16, 14, 0));
        expensiveRoute.setArrivalTime(LocalDateTime.of(2025, 3, 16, 17, 0));
        expensiveRoute.setTransportType("air");
        expensiveRoute.setTravelDuration(Duration.ofHours(3));
        expensiveRoute.setHasAvailableTickets(true);
        expensiveRoute.setMinPrice(15000);

        RouteDto expensiveRouteDto = new RouteDto();
        expensiveRouteDto.setId(8760821L);
        expensiveRouteDto.setDepartureCity("Москва");
        expensiveRouteDto.setArrivalCity("Новосибирск");
        expensiveRouteDto.setDepartureTime(LocalDateTime.of(2025, 3, 16, 14, 0));
        expensiveRouteDto.setArrivalTime(LocalDateTime.of(2025, 3, 16, 17, 0));
        expensiveRouteDto.setTravelDuration(Duration.ofHours(3));
        expensiveRouteDto.setTransportType("air");
        expensiveRouteDto.setHasAvailableTickets(true);
        expensiveRouteDto.setMinPrice(15000);

        List<Route> mockRoutes = Arrays.asList(moscowToNovosibirsk, expensiveRoute);
        when(routeRepository.findDirectRoutes(any(), any(), eq(departureCity), eq(arrivalCity), isNull()))
                .thenReturn(mockRoutes);
        when(routeMapper.toDto(moscowToNovosibirsk)).thenReturn(moscowToNovosibirskDto);
        when(routeMapper.toDto(expensiveRoute)).thenReturn(expensiveRouteDto);
        when(redisTemplate.opsForSet()).thenReturn(mock(SetOperations.class));

        // Act
        List<List<RouteDto>> result = routeService.findRoutes(
                departureCity, arrivalCity, startTime, SortCriteria.CHEAPEST,
                null, true, false);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size(), "Ожидается два маршрута");
        assertEquals(1, result.get(0).size(), "Первый маршрут должен содержать один сегмент");
        assertEquals(1, result.get(1).size(), "Второй маршрут должен содержать один сегмент");
        assertEquals(moscowToNovosibirskDto, result.get(0).get(0), "Первый маршрут должен быть дешевле");
        assertEquals(expensiveRouteDto, result.get(1).get(0), "Второй маршрут должен быть дороже");

        verify(routeMapper, times(2)).toDto(any());
    }

    @Test
    void testGetDirectRoutesGroupedByDate() {
        // Arrange
        String departureCity = "Москва";
        String arrivalCity = "Новосибирск";
        String transportType = "air";

        List<Route> mockRoutes = Collections.singletonList(moscowToNovosibirsk);
        when(routeRepository.findDirectRoutesWithSort(eq(departureCity), eq(arrivalCity), any(), any(), eq(transportType), any()))
                .thenReturn(mockRoutes);
        when(routeMapper.toDto(moscowToNovosibirsk)).thenReturn(moscowToNovosibirskDto);
        when(redisTemplate.opsForSet()).thenReturn(mock(SetOperations.class));

        // Act
        Map<LocalDate, List<RouteDto>> result = routeService.getDirectRoutesGroupedByDate(
                departureCity, arrivalCity, transportType, SortCriteria.DEFAULT);

        // Assert
        LocalDate expectedKey = LocalDate.of(2025, 3, 16); // Дата отправления
        assertNotNull(result);
        assertEquals(1, result.size(), "Ожидается одна группа маршрутов");
        assertTrue(result.containsKey(expectedKey), "Ключ должен соответствовать дате отправления 2025-03-16");
        assertEquals(1, result.get(expectedKey).size(), "Ожидается один маршрут в группе");
        assertEquals(moscowToNovosibirskDto, result.get(expectedKey).get(0), "Маршрут должен совпадать с ожидаемым DTO");
    }

    @Test
    void testCalculateTotalPrice() throws Exception {
        // Arrange
        Route route2 = new Route();
        route2.setMinPrice(5000);
        List<Route> path = Arrays.asList(moscowToNovosibirsk, route2);

        // Act
        java.lang.reflect.Method method = RouteService.class.getDeclaredMethod("calculateTotalPrice", List.class);
        method.setAccessible(true);
        int totalPrice = (int) method.invoke(routeService, path);

        // Assert
        assertEquals(15000, totalPrice, "Общая стоимость должна быть 10000 + 5000 = 15000");
    }

    @Test
    void testCalculateTotalDuration() throws Exception {
        // Arrange
        Route route2 = new Route();
        route2.setTravelDuration(Duration.ofHours(2));
        route2.setDepartureTime(moscowToNovosibirsk.getArrivalTime().plusHours(1)); // 16:00
        route2.setArrivalTime(moscowToNovosibirsk.getArrivalTime().plusHours(3));   // 18:00
        List<Route> path = Arrays.asList(moscowToNovosibirsk, route2);

        // Act
        java.lang.reflect.Method method = RouteService.class.getDeclaredMethod("calculateTotalDuration", List.class);
        method.setAccessible(true);
        long totalDuration = (long) method.invoke(routeService, path);

        // Assert
        assertEquals(360, totalDuration, "Общая продолжительность должна быть 3 часа + 1 час ожидания + 2 часа = 360 минут");
    }
}