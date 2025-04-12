package com.example.JourneyHub.repository;

import com.example.JourneyHub.model.entity.Ticket;
import com.example.JourneyHub.model.entity.TicketId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, TicketId> {

    @Query("SELECT t FROM Ticket t JOIN FETCH t.route r WHERE r.id IN :routeIds")
    List<Ticket> findByRoute_IdIn(@Param("routeIds") List<Long> routeIds);

    @Query(value = "SELECT EXISTS (SELECT 1 FROM tickets t WHERE t.route_id = :routeId AND t.passenger_id IS NULL)", nativeQuery = true)
    boolean existsAvailableByRouteId(@Param("routeId") Long routeId);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.route WHERE t.user.id = :userId " +
            "AND (t.route.departureTime >= CURRENT_TIMESTAMP OR :showHistory = true) " +
            "ORDER BY t.route.departureTime ASC")
    List<Ticket> findTicketsWithRouteByUserId(@Param("userId") Long userId, @Param("showHistory") boolean showHistory);

    @Query("SELECT t FROM Ticket t JOIN FETCH t.route WHERE t.passenger.id = :passengerId " +
            "AND (t.route.departureTime >= CURRENT_TIMESTAMP OR :showHistory = true) " +
            "ORDER BY t.route.departureTime ASC")
    List<Ticket> findTicketsWithRouteByPassengerId(@Param("passengerId") Long passengerId, @Param("showHistory") boolean showHistory);


}