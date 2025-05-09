package com.example.JourneyHub.service;

import com.example.JourneyHub.exception.DuplicateEmailException;
import com.example.JourneyHub.exception.InvalidPasswordException;
import com.example.JourneyHub.model.dto.UserDto;
import com.example.JourneyHub.model.dto.UserRegistrationDto;
import com.example.JourneyHub.model.entity.User;
import com.example.JourneyHub.model.mapper.UserMapper;
import com.example.JourneyHub.repository.UserRepository;
import com.example.JourneyHub.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    public UserDto registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new DuplicateEmailException("Пользователь с таким email уже существует");
        }
        User user = new User();
        user.setName(registrationDto.getName());
        user.setSurname(registrationDto.getSurname());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(new BCryptPasswordEncoder().encode(registrationDto.getPassword()));
        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }
    public UserDetails login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email " + email + " не найден"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidPasswordException("Неверный пароль для пользователя " + email);
        }

        return new CustomUserDetails(user);
    }
    public UserDto getUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return userMapper.toDto(currentUser);
    }
}