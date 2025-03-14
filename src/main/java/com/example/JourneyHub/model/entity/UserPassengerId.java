package com.example.JourneyHub.model.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Data
@Embeddable
public class UserPassengerId implements Serializable {
    private Long userId;
    private Long passengerId;

    public UserPassengerId() {
    }

    public UserPassengerId(Long userId, Long passengerId) {
        this.userId = userId;
        this.passengerId = passengerId;
    }
}