package com.example.JourneyHub.service;

import com.example.JourneyHub.model.dto.RouteDto;
import com.example.JourneyHub.model.entity.Route;
import com.example.JourneyHub.model.enums.SortCriteria;
import com.example.JourneyHub.model.mapper.RouteMapper;

import com.example.JourneyHub.service.route.RouteFinder;
import com.example.JourneyHub.utils.CacheUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RouteService {


        private final RouteMapper routeMapper;
        private final ObjectMapper objectMapper;
        private final RedisTemplate<String, Object> redisTemplate;
        private final RouteFinder routeFinder;

        @Cacheable(value = "routesSearch", key = "#departureCity + ',' + #arrivalCity + ',' + #startTime + ',' + #directOnly + ',' + #multiStop + ',' + (#sort != null ? #sort.name() : 'DEFAULT') + ',' + (#transportType != null ? #transportType : 'null')")
        @Transactional(readOnly = true)
        public List<List<RouteDto>> findRoutes(String departureCity, String arrivalCity,
                                               LocalDateTime startTime, SortCriteria sort,
                                               String transportType, boolean directOnly, boolean multiStop) {
                List<List<Route>> routes = routeFinder.findRoutes(departureCity, arrivalCity, startTime, sort, transportType, directOnly, multiStop);
                List<List<RouteDto>> result = routes.stream()
                        .map(routeList -> routeList.stream().map(routeMapper::toDto).collect(Collectors.toList()))
                        .collect(Collectors.toList());

                String cacheKey = "routesSearch::" + departureCity + "," + arrivalCity + "," + startTime + "," + directOnly + "," + multiStop + "," +
                        (sort != null ? sort.name() : "DEFAULT") + "," + (transportType != null ? transportType : "null");
                CacheUtils.updateRouteIndex(result, redisTemplate, objectMapper, cacheKey);

                return result;
        }


        @Cacheable(value = "routesDirect", key = "{#departureCity, #arrivalCity, #transportType, #sortCriteria}")
        public Map<LocalDate, List<RouteDto>> getDirectRoutesGroupedByDate(String departureCity, String arrivalCity,
                                                                           String transportType, SortCriteria sortCriteria) {
                String sortCriteriaStr = sortCriteria != null ? sortCriteria.name() : SortCriteria.DEFAULT.name();
                String effectiveTransportType = (transportType == null || transportType.trim().isEmpty()) ? "mix" : transportType.toLowerCase();


                Map<LocalDate, List<Route>> routesByDate = routeFinder.findDirectRoutesGroupedByDate(
                        departureCity, arrivalCity, effectiveTransportType, sortCriteria);


                Map<LocalDate, List<RouteDto>> result = routesByDate.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().stream()
                                        .map(routeMapper::toDto)
                                        .collect(Collectors.toList()),
                                (v1, v2) -> v1,
                                TreeMap::new
                        ));


                String cacheKey = "routesDirect::" + departureCity + "," + arrivalCity + "," + transportType + "," + sortCriteriaStr;
                CacheUtils.updateRouteIndex(
                        result.values().stream().collect(Collectors.toList()),
                        redisTemplate, objectMapper, cacheKey
                );

                return result;
        }

}