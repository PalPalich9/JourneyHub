package com.example.JourneyHub.repository;

import com.example.JourneyHub.model.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    Optional<Passenger> findByPassportSeriesAndPassportNumber(String passportSeries, String passportNumber);}
