package com.example.JourneyHub.service;

import com.example.JourneyHub.exception.InvalidPassengerDataException;
import com.example.JourneyHub.exception.ResourceNotFoundException;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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

        if (passengerDto.getName() == null || passengerDto.getSurname() == null ||
                passengerDto.getPassportNumber() == null || passengerDto.getGender() == null ||
                passengerDto.getBirthDate() == null) {
            throw new InvalidPassengerDataException("Необходимо заполнить все обязательные поля");
        }

        String nameRegex = "^[А-Я][а-я]*$";
        if (!passengerDto.getName().matches(nameRegex) || !passengerDto.getSurname().matches(nameRegex) ||
                (passengerDto.getPatronymic() != null && !passengerDto.getPatronymic().isEmpty() && !passengerDto.getPatronymic().matches(nameRegex))) {
            throw new InvalidPassengerDataException("Имя, фамилия и отчество должны содержать только кириллицу, начинаться с заглавной буквы, без пробелов");
        }

        LocalDate birthDate = passengerDto.getBirthDate();
        LocalDate currentDate = LocalDate.now();

        int currentYear = currentDate.getYear();
        if (birthDate.getYear() < 1860 || birthDate.getYear() > currentYear) {
            throw new InvalidPassengerDataException("Год рождения должен быть между 1860 и " + currentYear);
        }

        if (birthDate.isAfter(currentDate)) {
            throw new InvalidPassengerDataException("Дата рождения не может быть позже текущей даты");
        }

        int age = currentYear - birthDate.getYear();
        String passportSeries = passengerDto.getPassportSeries();
        String passportNumber = passengerDto.getPassportNumber();

        if (!passportNumber.matches("\\d{6}")) {
            throw new InvalidPassengerDataException("Номер паспорта должен содержать ровно 6 цифр");
        }

        if (age < 14) {
            if (passportSeries != null && !passportSeries.isEmpty()) {
                throw new InvalidPassengerDataException("Пассажирам младше 14 лет нельзя вводить серию");
            }
        }

        if (age >= 14) {
            if (passportSeries == null || !passportSeries.matches("\\d{4}")) {
                throw new InvalidPassengerDataException("Серия паспорта обязательна для возраста 14+ и должна содержать ровно 4 цифры");
            }
        }

        Passenger existingPassenger = passengerRepository.findByPassportSeriesAndPassportNumber(passengerDto.getPassportSeries(), passengerDto.getPassportNumber())
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
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Связь между пользователем " + userId + " и пассажиром " + passengerId + " не найдена",
                        "PASSENGER_LINK_NOT_FOUND"));

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
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Связь между пользователем " + userId + " и пассажиром " + passengerId + " не найдена",
                        "PASSENGER_LINK_NOT_FOUND"));

        return passengerMapper.toDto(userPassenger.getPassenger());
    }
}