package com.example.JourneyHub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatDto {
    private boolean isAvailable;
    private int seatNumber;
    private int price;
    private String ticketType;
}