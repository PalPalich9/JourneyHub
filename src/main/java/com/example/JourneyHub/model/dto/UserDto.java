package com.example.JourneyHub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;


import java.util.Set;

@Getter
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private Set<Long> passengerIds;
}
