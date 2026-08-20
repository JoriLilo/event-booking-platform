package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.AuthResponse;
import com.example.EventBookingPlatform.dto.UserLoginRequest;
import com.example.EventBookingPlatform.dto.UserRegisterRequest;
import com.example.EventBookingPlatform.dto.UserRegisterResponse;
import com.example.EventBookingPlatform.repository.UserRepository;
import com.example.EventBookingPlatform.security.JwtUtil;
import com.example.EventBookingPlatform.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    public ResponseEntity<UserRegisterResponse> register(@RequestBody UserRegisterRequest userRegisterRequest) {

        return new ResponseEntity<>(userService.register(userRegisterRequest), HttpStatus.CREATED);
    }

    @PostMapping("/register-organizer")
    public ResponseEntity<UserRegisterResponse> registerOrganizer(@RequestBody UserRegisterRequest userRegisterRequest) {
        return new ResponseEntity<>(userService.registerOrganizer(userRegisterRequest), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody UserLoginRequest request) {
        return new ResponseEntity<>(userService.login(request), HttpStatus.OK);
    }
}
