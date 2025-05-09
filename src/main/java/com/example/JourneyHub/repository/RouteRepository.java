package com.example.JourneyHub.repository;

import com.example.JourneyHub.model.entity.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {



    @Query(value = "SELECT * FROM routes r WHERE r.departure_time >= :minDepartureTime " +
            "AND r.departure_time <= :maxDepartureTime " +
            "AND r.departure_city != :arrivalCity " +
            "AND (:transportType IS NULL OR r.transport_type = :transportType) " +
            "AND (r.departure_city != :departureCity OR " +
            "     (r.departure_time >= :minDepartureTime AND r.departure_time <= :maxDepartureTimeForStartCity))",
            nativeQuery = true)
    Stream<Route> streamRoutesInTimeRange(
            @Param("minDepartureTime") LocalDateTime minDepartureTime,
            @Param("maxDepartureTime") LocalDateTime maxDepartureTime,
            @Param("arrivalCity") String arrivalCity,
            @Param("transportType") String transportType,
            @Param("departureCity") String departureCity,
            @Param("maxDepartureTimeForStartCity") LocalDateTime maxDepartureTimeForStartCity);


}