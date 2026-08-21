package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.AuthResponse;
import com.example.EventBookingPlatform.dto.UserLoginRequest;
import com.example.EventBookingPlatform.dto.UserRegisterRequest;
import com.example.EventBookingPlatform.dto.UserRegisterResponse;
import com.example.EventBookingPlatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserRegisterResponse> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {

        return new ResponseEntity<>(userService.register(userRegisterRequest), HttpStatus.CREATED);
    }

    @PostMapping("/register-organizer")
    public ResponseEntity<UserRegisterResponse> registerOrganizer(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        return new ResponseEntity<>(userService.registerOrganizer(userRegisterRequest), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return new ResponseEntity<>(userService.login(request), HttpStatus.OK);
    }
}