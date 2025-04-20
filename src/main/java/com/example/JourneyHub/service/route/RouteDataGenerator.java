package com.example.JourneyHub.service.route;

import com.example.JourneyHub.model.entity.Route;
import com.example.JourneyHub.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class RouteDataGenerator {

    private final RouteRepository routeRepository;

    private static final List<String> CITIES = Arrays.asList(
            "Волгоград", "Воронеж", "Екатеринбург", "Ижевск", "Казань", "Краснодар", "Красноярск",
            "Москва", "Нижний Новгород", "Новосибирск", "Омск", "Пермь", "Ростов-На-Дону", "Самара",
            "Санкт-Петербург", "Саратов", "Тольятти", "Тюмень", "Уфа", "Челябинск", "Якутск"
    );
    private static final List<String> POPULAR_CITIES = Arrays.asList(
            "Москва", "Санкт-Петербург", "Екатеринбург", "Новосибирск", "Казань", "Ростов-На-Дону"
    );
    private static final List<String> TRANSPORT_TYPES = Arrays.asList("air", "train", "bus");
    private static final LocalDateTime START_DATE = LocalDateTime.of(2025, 4, 10, 0, 0);
    private static final LocalDateTime END_DATE = LocalDateTime.of(2026, 4, 10, 23, 50);
    private static final int TOTAL_ROUTES = 1_000_000;
    private static final int BATCH_SIZE = 1000;

    private final Random random = new Random();

    public void generateRoutes() {
        if (routeRepository.count() > 0) {
            System.out.println("Routes already exist in the database. Skipping generation.");
            return;
        }

        List<Route> batch = new ArrayList<>(BATCH_SIZE);
        long tripId = 1_000_000L;

        for (int i = 0; i < TOTAL_ROUTES; ) {
            long daysBetween = ChronoUnit.DAYS.between(START_DATE, END_DATE);
            LocalDateTime departureTime = START_DATE.plusDays(random.nextInt((int) daysBetween))
                    .withHour(random.nextInt(24))
                    .withMinute((random.nextInt(6) * 10))
                    .withSecond(0)
                    .withNano(0);

            String transportType = TRANSPORT_TYPES.get(random.nextInt(TRANSPORT_TYPES.size()));
            String departureCity = getRandomCity(transportType);
            String arrivalCity = getRandomCityExcluding(departureCity);

            if (transportType.equals("air")) {
                Route route = createSingleRoute(departureCity, arrivalCity, departureTime, transportType, tripId);
                batch.add(route);
                i++;
                tripId++;
            } else {
                if (random.nextDouble() < 0.1) {
                    List<Route> chain = createRouteChain(departureCity, transportType, departureTime, tripId);
                    batch.addAll(chain);
                    i += chain.size();
                    tripId++;
                } else {
                    Route route = createSingleRoute(departureCity, arrivalCity, departureTime, transportType, tripId);
                    batch.add(route);
                    i++;
                    tripId++;
                }
            }

            if (batch.size() >= BATCH_SIZE) {
                routeRepository.saveAll(batch);
                batch.clear();
                System.out.println("Saved batch of " + BATCH_SIZE + " routes. Total routes so far: " + i);
            }
        }

        if (!batch.isEmpty()) {
            routeRepository.saveAll(batch);
            System.out.println("Saved final batch of " + batch.size() + " routes.");
        }
    }

    private String getRandomCity(String transportType) {
        if (random.nextDouble() < 0.7) {
            return POPULAR_CITIES.get(random.nextInt(POPULAR_CITIES.size()));
        }
        return CITIES.get(random.nextInt(CITIES.size()));
    }

    private String getRandomCityExcluding(String excludeCity) {
        String city;
        do {
            city = CITIES.get(random.nextInt(CITIES.size()));
        } while (city.equals(excludeCity));
        return city;
    }

    private Route createSingleRoute(String departureCity, String arrivalCity, LocalDateTime departureTime,
                                    String transportType, long tripId) {
        LocalDateTime arrivalTime = calculateArrivalTime(departureCity, arrivalCity, departureTime, transportType);
        return new Route(null, departureCity, arrivalCity, departureTime, arrivalTime, null, 0, transportType, true, tripId);
    }

    private List<Route> createRouteChain(String startCity, String transportType, LocalDateTime startTime, long tripId) {
        List<Route> chain = new ArrayList<>();
        String currentCity = startCity;
        LocalDateTime currentTime = startTime;
        int chainLength = random.nextInt(3) + 2;

        for (int j = 0; j < chainLength - 1; j++) {
            String nextCity = getRandomCityExcluding(currentCity);
            LocalDateTime arrivalTime = calculateArrivalTime(currentCity, nextCity, currentTime, transportType);
            Route route = new Route(null, currentCity, nextCity, currentTime, arrivalTime, null, 0, transportType, true, tripId);
            chain.add(route);

            int waitMinutes = 10 * (1 + random.nextInt(4));
            currentTime = arrivalTime.plusMinutes(waitMinutes);
            currentCity = nextCity;
        }
        return chain;
    }

    private LocalDateTime calculateArrivalTime(String departureCity, String arrivalCity,
                                               LocalDateTime departureTime, String transportType) {
        int distance = estimateDistance(departureCity, arrivalCity);
        int travelTimeMinutes;

        switch (transportType) {
            case "air":
                travelTimeMinutes = (int) (distance / 15.0) + 30; // 900 км/ч + 30 мин
                break;
            case "train":
                travelTimeMinutes = (int) (distance / 1.33); // 80 км/ч
                break;
            case "bus":
                travelTimeMinutes = (int) (distance / 1.0); // 60 км/ч
                break;
            default:
                travelTimeMinutes = 60;
        }

        int roundedTravelTime = ((travelTimeMinutes + 9) / 10) * 10;
        return departureTime.plusMinutes(roundedTravelTime).withSecond(0).withNano(0);
    }

    private int estimateDistance(String city1, String city2) {
        if (city1.equals("Москва") && city2.equals("Санкт-Петербург") || city2.equals("Москва") && city1.equals("Санкт-Петербург")) {
            return 650;
        } else if (city1.equals("Екатеринбург") && city2.equals("Новосибирск") || city2.equals("Екатеринбург") && city1.equals("Новосибирск")) {
            return 1400;
        } else {
            return 300 + random.nextInt(2000);
        }
    }
}