package com.example.JourneyHub.model.dto;



import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
@Getter
@AllArgsConstructor
public class PassengerDto {
    private Long id;
    private String name;
    private String surname;
    private String patronymic;
    private String passportNumber;
    private String gender;
    private LocalDate birthDate;
}
