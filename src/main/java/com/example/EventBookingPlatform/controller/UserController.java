package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.UserRegisterRequest;
import com.example.EventBookingPlatform.dto.UserRegisterResponse;
import com.example.EventBookingPlatform.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserRegisterResponse> register(@RequestBody UserRegisterRequest userRegisterRequest) {

        return new ResponseEntity<>(userService.register(userRegisterRequest), HttpStatus.CREATED);

    }
}
