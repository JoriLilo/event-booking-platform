package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.AuthResponse;
import com.example.EventBookingPlatform.dto.UserLoginRequest;
import com.example.EventBookingPlatform.dto.UserRegisterRequest;
import com.example.EventBookingPlatform.dto.UserRegisterResponse;
import com.example.EventBookingPlatform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and login endpoints")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register as Attendee",
            description = "Create a new attendee account. Attendees can book events and write reviews."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Attendee registered successfully",
                    content = @Content(schema = @Schema(implementation = UserRegisterResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request (duplicate email, missing fields)"
            )
    })
    public ResponseEntity<UserRegisterResponse> register(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        return new ResponseEntity<>(userService.register(userRegisterRequest), HttpStatus.CREATED);
    }

    @PostMapping("/register-organizer")
    @Operation(
            summary = "Register as Organizer",
            description = "Create a new organizer account. Organizers can create and manage events."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Organizer registered successfully",
                    content = @Content(schema = @Schema(implementation = UserRegisterResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request (duplicate email, missing fields)"
            )
    })
    public ResponseEntity<UserRegisterResponse> registerOrganizer(@Valid @RequestBody UserRegisterRequest userRegisterRequest) {
        return new ResponseEntity<>(userService.registerOrganizer(userRegisterRequest), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(
            summary = "User Login",
            description = "Authenticate user with email and password. Returns JWT token for subsequent requests."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful, JWT token returned",
                    content = @Content(schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password"
            )
    })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody UserLoginRequest request) {
        return new ResponseEntity<>(userService.login(request), HttpStatus.OK);
    }
}