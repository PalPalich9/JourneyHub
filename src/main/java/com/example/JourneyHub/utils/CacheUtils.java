package com.example.JourneyHub.utils;

import com.example.JourneyHub.model.dto.RouteDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CacheUtils {
    public static void updateRouteIndex(List<List<RouteDto>> routes, RedisTemplate<String, Object> redisTemplate,
                                        ObjectMapper objectMapper, String cacheKey) {
        Set<Long> routeIds = routes.stream()
                .flatMap(List::stream)
                .flatMap(route -> route.getRouteIds().stream())
                .collect(Collectors.toSet());

        for (Long routeId : routeIds) {
            String routeIndexKey = "route:" + routeId;
            redisTemplate.opsForSet().add(routeIndexKey, cacheKey);
        }
    }
}
