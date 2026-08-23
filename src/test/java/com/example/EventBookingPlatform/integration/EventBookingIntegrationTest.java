package com.example.EventBookingPlatform.integration;

import com.example.EventBookingPlatform.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Authentication, authorization, and permission integration test using H2 in-memory DB (no Docker).
 * Tests role-based access control, registration validation, login flow, and permission boundaries.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EventBookingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String attendeeToken;
    private static String organizerToken;

    @Test
    @Order(1)
    void testRegisterAttendee() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("test_attendee");
        request.setEmail("attendee@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("test_attendee"))
                .andExpect(jsonPath("$.role").value("ATTENDEE"));
    }

    @Test
    @Order(2)
    void testRegisterOrganizer() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("test_organizer");
        request.setEmail("organizer@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register-organizer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ORGANIZER"));
    }

    @Test
    @Order(3)
    void testLoginAttendee() throws Exception {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("attendee@test.com");
        request.setPassword("password123");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn().getResponse().getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);
        attendeeToken = authResponse.getToken();
    }

    @Test
    @Order(4)
    void testLoginOrganizer() throws Exception {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("organizer@test.com");
        request.setPassword("password123");

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse authResponse = objectMapper.readValue(response, AuthResponse.class);
        organizerToken = authResponse.getToken();
    }

    @Test
    @Order(5)
    void testUnauthorizedAccessRejected() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(6)
    void testCreateEventAsNonOrganizerForbidden() throws Exception {
        EventRequest request = new EventRequest();
        request.setTitle("Should Fail Event");
        request.setStartDateTime(LocalDateTime.now().plusDays(5));
        request.setPrice(10);
        request.setTotalSeats(50);
        request.setVenueId(1L);

        mockMvc.perform(post("/api/event")
                        .header("Authorization", "Bearer " + attendeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(7)
    void testCreateEventInvalidDataRejected() throws Exception {
        EventRequest request = new EventRequest();
        request.setTitle("");
        request.setStartDateTime(LocalDateTime.now().plusDays(5));
        request.setPrice(10);
        request.setTotalSeats(50);
        request.setVenueId(1L);

        mockMvc.perform(post("/api/event")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(8)
    void testCreateVenueAsNonAdminForbidden() throws Exception {
        VenueRequest request = new VenueRequest();
        request.setName("Test Arena");
        request.setAddress("123 Test Street");
        request.setCity("Test City");
        request.setCapacity(200);

        mockMvc.perform(post("/api/venue")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(9)
    void testGetAllEventsPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/api/event"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(10)
    void testGetAllVenuesPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/api/venue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(11)
    void testCreateCategoryAsNonAdminForbidden() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setCategoryName("Music");

        mockMvc.perform(post("/api/category")
                        .header("Authorization", "Bearer " + attendeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(12)
    void testGetAllCategoriesPubliclyAccessible() throws Exception {
        mockMvc.perform(get("/api/category"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(13)
    void testDuplicateEmailRegistrationRejected() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("duplicate_user");
        request.setEmail("attendee@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(14)
    void testLoginWithWrongPasswordRejected() throws Exception {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail("attendee@test.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(15)
    void testGetBookingsForUserRequiresAuth() throws Exception {
        mockMvc.perform(get("/api/booking/user")
                        .header("Authorization", "Bearer " + attendeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}