package com.example.JourneyHub.repository;

import com.example.JourneyHub.model.entity.UserPassenger;
import com.example.JourneyHub.model.entity.UserPassengerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPassengerRepository extends JpaRepository<UserPassenger, UserPassengerId> {
    boolean existsById(UserPassengerId id);
}