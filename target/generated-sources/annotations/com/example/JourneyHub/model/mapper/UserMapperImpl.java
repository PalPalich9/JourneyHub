package com.example.JourneyHub.model.mapper;

import com.example.JourneyHub.model.dto.UserDto;
import com.example.JourneyHub.model.entity.User;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-03-14T11:55:40+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 22.0.1 (Oracle Corporation)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        if ( user == null ) {
            return null;
        }

        Set<Long> passengerIds = null;
        Long id = null;
        String name = null;
        String surname = null;
        String email = null;

        passengerIds = mapPassengerIds( user.getUserPassengers() );
        id = user.getId();
        name = user.getName();
        surname = user.getSurname();
        email = user.getEmail();

        UserDto userDto = new UserDto( id, name, surname, email, passengerIds );

        return userDto;
    }

    @Override
    public User toEntity(UserDto userDto) {
        if ( userDto == null ) {
            return null;
        }

        User user = new User();

        user.setId( userDto.getId() );
        user.setName( userDto.getName() );
        user.setSurname( userDto.getSurname() );
        user.setEmail( userDto.getEmail() );

        return user;
    }
}
