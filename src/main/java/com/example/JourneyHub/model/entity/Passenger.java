package com.example.JourneyHub.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "passengers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Passenger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String surname;
    private String patronymic;

    @Column(name = "passport_number", unique = true, nullable = false)
    private String passportNumber;
    @Column(name = "gender", nullable = false)
    private String gender;
    private LocalDate birthDate;
}