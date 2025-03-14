package com.example.JourneyHub.model.mapper;

import com.example.JourneyHub.model.dto.PassengerCreationDto;
import com.example.JourneyHub.model.dto.PassengerDto;
import com.example.JourneyHub.model.entity.Passenger;
import java.time.LocalDate;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-03-14T01:38:25+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class PassengerMapperImpl implements PassengerMapper {

    @Override
    public PassengerDto toDto(Passenger passenger) {
        if ( passenger == null ) {
            return null;
        }

        Long id = null;
        String name = null;
        String surname = null;
        String patronymic = null;
        String passportNumber = null;
        String gender = null;
        LocalDate birthDate = null;

        id = passenger.getId();
        name = passenger.getName();
        surname = passenger.getSurname();
        patronymic = passenger.getPatronymic();
        passportNumber = passenger.getPassportNumber();
        gender = passenger.getGender();
        birthDate = passenger.getBirthDate();

        PassengerDto passengerDto = new PassengerDto( id, name, surname, patronymic, passportNumber, gender, birthDate );

        return passengerDto;
    }

    @Override
    public Passenger toEntity(PassengerCreationDto passengerCreationDto) {
        if ( passengerCreationDto == null ) {
            return null;
        }

        Passenger passenger = new Passenger();

        passenger.setName( passengerCreationDto.getName() );
        passenger.setSurname( passengerCreationDto.getSurname() );
        passenger.setPatronymic( passengerCreationDto.getPatronymic() );
        passenger.setPassportNumber( passengerCreationDto.getPassportNumber() );
        passenger.setGender( passengerCreationDto.getGender() );
        passenger.setBirthDate( passengerCreationDto.getBirthDate() );

        return passenger;
    }
}
