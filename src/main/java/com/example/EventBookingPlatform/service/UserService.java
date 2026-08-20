package com.example.EventBookingPlatform.service;

import com.example.EventBookingPlatform.dto.AuthResponse;
import com.example.EventBookingPlatform.dto.UserLoginRequest;
import com.example.EventBookingPlatform.dto.UserRegisterRequest;
import com.example.EventBookingPlatform.dto.UserRegisterResponse;
import com.example.EventBookingPlatform.entity.Role;
import com.example.EventBookingPlatform.entity.User;
import com.example.EventBookingPlatform.exception.UserNotFoundException;
import com.example.EventBookingPlatform.repository.UserRepository;
import com.example.EventBookingPlatform.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public UserRegisterResponse register(UserRegisterRequest request) {

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User userEntity = new User();
        userEntity.setUsername(request.getUsername());
        userEntity.setEmail(request.getEmail());
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        userEntity.setRole(Role.ATTENDEE);

        userRepository.save(userEntity);

        return userToUserRegisterResponse(userEntity);
    }

    public UserRegisterResponse registerOrganizer(UserRegisterRequest request) {

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User userEntity = new User();
        userEntity.setUsername(request.getUsername());
        userEntity.setEmail(request.getEmail());
        userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
        userEntity.setRole(Role.ORGANIZER);

        userRepository.save(userEntity);

        return userToUserRegisterResponse(userEntity);
    }

    public AuthResponse login(UserLoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );


        String token = jwtUtil.generateToken(request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().toString());
        return response;
    }

    public UserRegisterResponse getByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return userToUserRegisterResponse(user);

    }

    public UserRegisterResponse getById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));


        return userToUserRegisterResponse(user);
    }

    public UserRegisterResponse updateUser(Long id, UserRegisterRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return userToUserRegisterResponse(user);
    }

    public void deleteUserById(Long id){

        if(userRepository.findById(id).isPresent()){
            userRepository.deleteById(id);
        }else {
            throw new UserNotFoundException("User not found");
        }

    }

    public List<UserRegisterResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserRegisterResponse> responses = new ArrayList<>();

        for (User user : users) {

            responses.add( userToUserRegisterResponse(user));
        }
        return responses;
    }

    public UserRegisterResponse userToUserRegisterResponse(User user) {
        UserRegisterResponse response = new UserRegisterResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().toString());
        return response;
    }
}