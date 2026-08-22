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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserService userService;

    private UserRegisterRequest registerRequest;
    private UserLoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new UserRegisterRequest();
        registerRequest.setUsername("john_doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new UserLoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setPassword("encodedPassword");
        user.setRole(Role.ATTENDEE);
    }

    @Test
    void testRegisterSuccess() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserRegisterResponse response = userService.register(registerRequest);

        assertNotNull(response);
        assertEquals("john_doe", response.getUsername());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("ATTENDEE", response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterOrganizerSuccess() {
        user.setRole(Role.ORGANIZER);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserRegisterResponse response = userService.registerOrganizer(registerRequest);

        assertNotNull(response);
        assertEquals("ORGANIZER", response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateEmail() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () ->
                userService.register(registerRequest));
    }

    @Test
    void testRegisterWithNullUsername() {
        registerRequest.setUsername(null);

        assertThrows(IllegalArgumentException.class, () ->
                userService.register(registerRequest));
    }

    @Test
    void testRegisterWithInvalidEmail() {
        registerRequest.setEmail("invalidemail");

        assertThrows(IllegalArgumentException.class, () ->
                userService.register(registerRequest));
    }

    @Test
    void testRegisterWithWeakPassword() {
        registerRequest.setPassword("weak");

        assertThrows(IllegalArgumentException.class, () ->
                userService.register(registerRequest));
    }

    @Test
    void testLoginSuccess() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken("john@example.com", "password123"));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken("john@example.com")).thenReturn("mockedJwtToken");

        AuthResponse response = userService.login(loginRequest);

        assertNotNull(response);
        assertEquals("john_doe", response.getUsername());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("ATTENDEE", response.getRole());
        assertEquals("mockedJwtToken", response.getToken());
    }

    @Test
    void testLoginInvalidPassword() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () ->
                userService.login(loginRequest));
    }

    @Test
    void testGetByEmail() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        UserRegisterResponse response = userService.getByEmail("john@example.com");

        assertNotNull(response);
        assertEquals("john_doe", response.getUsername());
    }

    @Test
    void testGetByEmailNotFound() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                userService.getByEmail("john@example.com"));
    }

    @Test
    void testGetById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserRegisterResponse response = userService.getById(1L);

        assertNotNull(response);
        assertEquals("john_doe", response.getUsername());
    }

    @Test
    void testUpdateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        registerRequest.setPassword("newpassword123");
        UserRegisterResponse response = userService.updateUser(1L, registerRequest);

        assertNotNull(response);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUserById(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }
}