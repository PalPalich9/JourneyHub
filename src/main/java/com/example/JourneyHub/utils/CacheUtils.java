package com.example.JourneyHub.utils;

import com.example.JourneyHub.model.dto.RouteDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CacheUtils {

    public static void updateRouteIndex(List<List<RouteDto>> routes, RedisTemplate<String, Object> redisTemplate,
                                        ObjectMapper objectMapper, String cacheKey) {
        for (List<RouteDto> path : routes) {
            for (RouteDto route : path) {
                String routeIndexKey = "route:" + route.getId();
                redisTemplate.opsForSet().add(routeIndexKey, cacheKey);
            }
        }
    }


}