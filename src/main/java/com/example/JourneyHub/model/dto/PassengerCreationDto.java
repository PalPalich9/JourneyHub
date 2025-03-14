package com.example.JourneyHub.model.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PassengerCreationDto {
    @NotNull(message = "Name is required")
    private String name;

    @NotNull(message = "Surname is required")
    private String surname;

    private String patronymic;

    @NotNull(message = "Passport number is required")
    private String passportNumber;

    @NotNull(message = "Gender is required")
    @Pattern(regexp = "^[MF]$", message = "Gender must be 'M' or 'F'")
    private String gender;

    @NotNull(message = "Birth date is required")
    private LocalDate birthDate;

}