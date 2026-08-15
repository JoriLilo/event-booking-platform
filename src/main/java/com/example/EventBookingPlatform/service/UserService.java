package com.example.EventBookingPlatform.service;

import com.example.EventBookingPlatform.dto.AuthResponse;
import com.example.EventBookingPlatform.dto.UserLoginRequest;
import com.example.EventBookingPlatform.dto.UserRegisterRequest;
import com.example.EventBookingPlatform.dto.UserRegisterResponse;
import com.example.EventBookingPlatform.entity.Role;
import com.example.EventBookingPlatform.entity.User;
import com.example.EventBookingPlatform.exception.UserNotFoundException;
import com.example.EventBookingPlatform.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

        //create entity
        User userEntity = new User();
        userEntity.setUsername(request.getUsername());
        userEntity.setEmail(request.getEmail());
        userEntity.setPassword(request.getPassword()); // TODO: Hash this with BCrypt
        userEntity.setRole(Role.ATTENDEE);

        userRepository.save(userEntity);

        // User response is used to return
        UserRegisterResponse response = new UserRegisterResponse();
        response.setId(userEntity.getId());
        response.setUsername(userEntity.getUsername());
        response.setEmail(userEntity.getEmail());
        response.setRole(userEntity.getRole().toString());

        return response;
    }

    /*TODO
        configure jwt before doing log in
     */
//    public AuthResponse login(UserLoginRequest userLoginRequest) {
//
//    }

    public User findByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return user;

    }

    public User findById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return user;
    }

    public UserRegisterResponse  updateUser(Long id, UserRegisterRequest request) {

        User user = findById(id);
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if(request.getUsername() != user.getUsername()){
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() == null || !request.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (request.getEmail() != user.getEmail()) {
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        if(request.getPassword() != user.getPassword()){
            user.setPassword(request.getPassword());
        }


        userRepository.save(user);

        UserRegisterResponse response = new UserRegisterResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().toString());
        return response;

    }
}