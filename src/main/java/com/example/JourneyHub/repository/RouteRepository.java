package com.example.JourneyHub.repository;

import com.example.JourneyHub.model.entity.Route;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    @Query(value = "SELECT * FROM routes r WHERE r.departure_city = :departureCity " +
            "AND r.arrival_city = :arrivalCity " +
            "AND r.departure_time >= :startTime " +
            "AND r.departure_time <= :maxDepartureTime " +
            "AND (:transportType IS NULL OR r.transport_type = :transportType)",
            nativeQuery = true)
    List<Route> findDirectRoutes(
            @Param("startTime") LocalDateTime startTime,
            @Param("maxDepartureTime") LocalDateTime maxDepartureTime,
            @Param("departureCity") String departureCity,
            @Param("arrivalCity") String arrivalCity,
            @Param("transportType") String transportType);

    @Query(value = "SELECT * FROM routes r WHERE r.departure_time >= :minDepartureTime " +
            "AND r.departure_time <= :maxDepartureTime " +
            "AND r.departure_city != :arrivalCity " +
            "AND (:transportType IS NULL OR r.transport_type = :transportType)",
            nativeQuery = true)
    Stream<Route> streamRoutesInTimeRange(
            @Param("minDepartureTime") LocalDateTime minDepartureTime,
            @Param("maxDepartureTime") LocalDateTime maxDepartureTime,
            @Param("arrivalCity") String arrivalCity,
            @Param("transportType") String transportType);



    @Query(value = "SELECT * FROM routes r " +
            "WHERE r.departure_city = :departureCity " +
            "AND r.arrival_city = :arrivalCity " +
            "AND r.departure_time >= :startDate " +
            "AND r.departure_time <= :endDate " +
            "AND (:transportType IS NULL OR r.transport_type = :transportType) " +
            "ORDER BY DATE(r.departure_time) ASC, " +
            "CASE :sortCriteria " +
            "   WHEN 'CHEAPEST' THEN r.min_price " +
            "   WHEN 'AVAILABILITY' THEN CASE WHEN r.has_available_tickets THEN 0 ELSE 1 END " +
            "END ASC, " +
            "CASE :sortCriteria " +
            "   WHEN 'EXPENSIVE' THEN r.min_price " +
            "END DESC, " +
            "CASE :sortCriteria " +
            "   WHEN 'DURATION' THEN CAST('1970-01-01 ' || r.travel_duration AS TIMESTAMP) " +
            "   WHEN 'DEFAULT' THEN r.departure_time " +
            "   ELSE r.departure_time " +
            "END ASC", nativeQuery = true)
    List<Route> findDirectRoutesWithSort(
            @Param("departureCity") String departureCity,
            @Param("arrivalCity") String arrivalCity,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("transportType") String transportType,
            @Param("sortCriteria") String sortCriteria);


}