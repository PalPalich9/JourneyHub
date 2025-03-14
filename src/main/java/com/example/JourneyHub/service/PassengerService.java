package com.example.JourneyHub.service;

import com.example.JourneyHub.model.dto.PassengerCreationDto;
import com.example.JourneyHub.model.dto.PassengerDto;
import com.example.JourneyHub.model.entity.Passenger;
import com.example.JourneyHub.model.entity.User;
import com.example.JourneyHub.model.entity.UserPassenger;
import com.example.JourneyHub.model.entity.UserPassengerId;
import com.example.JourneyHub.model.mapper.PassengerMapper;
import com.example.JourneyHub.repository.PassengerRepository;
import com.example.JourneyHub.repository.UserPassengerRepository;
import com.example.JourneyHub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PassengerService {

    private final PassengerRepository passengerRepository;
    private final UserRepository userRepository;
    private final UserPassengerRepository userPassengerRepository;
    private final PassengerMapper passengerMapper;

    public PassengerDto addPassenger(Long userId, PassengerCreationDto passengerDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        if (!currentUser.getId().equals(userId)) {
            throw new SecurityException("У вас нет прав для добавления пассажира для другого пользователя");
        }

        Passenger existingPassenger = passengerRepository.findByPassportNumber(passengerDto.getPassportNumber())
                .orElseGet(() -> {
                    Passenger newPassenger = passengerMapper.toEntity(passengerDto);
                    return passengerRepository.save(newPassenger);
                });

        UserPassengerId userPassengerId = new UserPassengerId(currentUser.getId(), existingPassenger.getId());
        if (!userPassengerRepository.existsById(userPassengerId)) {

            UserPassenger userPassenger = new UserPassenger();
            userPassenger.setUserId(currentUser.getId());
            userPassenger.setPassengerId(existingPassenger.getId());
            userPassenger.setUser(currentUser);
            userPassenger.setPassenger(existingPassenger);
            userPassengerRepository.save(userPassenger);
        }

        return passengerMapper.toDto(existingPassenger);
    }

    public List<PassengerDto> getPassengersByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        if (!currentUser.getId().equals(userId)) {
            throw new SecurityException("У вас нет прав для просмотра пассажиров другого пользователя");
        }

        return user.getUserPassengers().stream()
                .map(up -> passengerMapper.toDto(up.getPassenger()))
                .collect(Collectors.toList());
    }
    public void removePassenger(Long userId, Long passengerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        if (!currentUser.getId().equals(userId)) {
            throw new SecurityException("У вас нет прав для удаления связи с пассажиром другого пользователя");
        }

        UserPassengerId userPassengerId = new UserPassengerId(userId, passengerId);

        UserPassenger userPassenger = userPassengerRepository.findById(userPassengerId)
                .orElseThrow(() -> new IllegalArgumentException("Связь между пользователем " + userId + " и пассажиром " + passengerId + " не найдена"));

        userPassengerRepository.delete(userPassenger);
    }
    public PassengerDto getPassengerByUserAndId(Long userId, Long passengerId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        if (!currentUser.getId().equals(userId)) {
            throw new SecurityException("У вас нет прав для просмотра пассажира другого пользователя");
        }

        UserPassengerId userPassengerId = new UserPassengerId(userId, passengerId);
        UserPassenger userPassenger = userPassengerRepository.findById(userPassengerId)
                .orElseThrow(() -> new IllegalArgumentException("Связь между пользователем " + userId + " и пассажиром " + passengerId + " не найдена"));

        return passengerMapper.toDto(userPassenger.getPassenger());
    }
}