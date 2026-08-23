package com.example.EventBookingPlatform.integration;

import com.example.EventBookingPlatform.dto.*;
import com.example.EventBookingPlatform.entity.Role;
import com.example.EventBookingPlatform.entity.User;
import com.example.EventBookingPlatform.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Full booking flow integration test using H2 in-memory database (no Docker).
 * Tests: admin creates venue+category → organizer creates event → attendee books →
 * seats decrement → attendee cancels → seats restored.
 *
 * ADMIN is seeded directly via repository (matches spec: ADMIN seeded manually, not via /register).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FullBookingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static String adminToken;
    private static String organizerToken;
    private static String attendeeToken;
    private static Long venueId;
    private static Long categoryId;
    private static Long eventId;
    private static Long bookingId;

    @Test
    @Order(1)
    void setup_seedAdminAndRegisterUsers() throws Exception {
        // Seed ADMIN directly (matches your architecture: ADMIN seeded manually, not via /register)
        if (userRepository.findByEmail("admin@test.com").isEmpty()) {
            User admin = new User();
            admin.setUsername("test_admin");
            admin.setEmail("admin@test.com");
            admin.setPassword(passwordEncoder.encode("adminpass123"));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }

        // Register organizer
        UserRegisterRequest organizerReq = new UserRegisterRequest();
        organizerReq.setUsername("flow_organizer");
        organizerReq.setEmail("floworganizer@test.com");
        organizerReq.setPassword("password123");
        mockMvc.perform(post("/api/auth/register-organizer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(organizerReq)))
                .andExpect(status().isCreated());

        // Register attendee
        UserRegisterRequest attendeeReq = new UserRegisterRequest();
        attendeeReq.setUsername("flow_attendee");
        attendeeReq.setEmail("flowattendee@test.com");
        attendeeReq.setPassword("password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(attendeeReq)))
                .andExpect(status().isCreated());
    }

    @Test
    @Order(2)
    void setup_loginAllUsers() throws Exception {
        adminToken = login("admin@test.com", "adminpass123");
        organizerToken = login("floworganizer@test.com", "password123");
        attendeeToken = login("flowattendee@test.com", "password123");
    }

    private String login(String email, String password) throws Exception {
        UserLoginRequest request = new UserLoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(response, AuthResponse.class).getToken();
    }

    @Test
    @Order(3)
    void testAdminCreatesVenue() throws Exception {
        VenueRequest request = new VenueRequest();
        request.setName("Flow Test Arena");
        request.setAddress("456 Flow Street");
        request.setCity("Flow City");
        request.setCapacity(300);

        String response = mockMvc.perform(post("/api/venue")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Flow Test Arena"))
                .andReturn().getResponse().getContentAsString();

        VenueResponse venueResponse = objectMapper.readValue(response, VenueResponse.class);
        venueId = venueResponse.getId();
    }

    @Test
    @Order(4)
    void testAdminCreatesCategory() throws Exception {
        CategoryRequest request = new CategoryRequest();
        request.setCategoryName("Conference");

        String response = mockMvc.perform(post("/api/category")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        CategoryResponse categoryResponse = objectMapper.readValue(response, CategoryResponse.class);
        categoryId = categoryResponse.getId();
    }

    @Test
    @Order(5)
    void testOrganizerCreatesEvent() throws Exception {
        EventRequest request = new EventRequest();
        request.setTitle("Flow Test Conference");
        request.setDescription("Integration test event");
        request.setStartDateTime(LocalDateTime.now().plusDays(10));
        request.setEndDateTime(LocalDateTime.now().plusDays(10).plusHours(6));
        request.setPrice(50.0f);
        request.setTotalSeats(10);
        request.setVenueId(venueId);
        request.setCategoryIds(java.util.List.of(categoryId));

        String response = mockMvc.perform(post("/api/event")
                        .header("Authorization", "Bearer " + organizerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Flow Test Conference"))
                .andExpect(jsonPath("$.availableSeats").value(10))
                .andExpect(jsonPath("$.totalSeats").value(10))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andReturn().getResponse().getContentAsString();

        EventResponse eventResponse = objectMapper.readValue(response, EventResponse.class);
        eventId = eventResponse.getId();
    }

    @Test
    @Order(6)
    void testAttendeeBooksSeats() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setEventId(eventId);
        request.setSeatsBooked(3);

        String response = mockMvc.perform(post("/api/booking")
                        .header("Authorization", "Bearer " + attendeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seatsBooked").value(3))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andReturn().getResponse().getContentAsString();

        BookingResponse bookingResponse = objectMapper.readValue(response, BookingResponse.class);
        bookingId = bookingResponse.getId();
    }

    @Test
    @Order(7)
    void testAvailableSeatsDecrementedAfterBooking() throws Exception {
        mockMvc.perform(get("/api/event/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSeats").value(7))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @Order(8)
    void testCannotBookMoreSeatsThanAvailable() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setEventId(eventId);
        request.setSeatsBooked(100);

        mockMvc.perform(post("/api/booking")
                        .header("Authorization", "Bearer " + attendeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(9)
    void testEventSoldOutWhenSeatsExhausted() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setEventId(eventId);
        request.setSeatsBooked(7);

        mockMvc.perform(post("/api/booking")
                        .header("Authorization", "Bearer " + attendeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/event/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSeats").value(0))
                .andExpect(jsonPath("$.status").value("SOLD_OUT"));
    }

    @Test
    @Order(10)
    void testCannotBookSoldOutEvent() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setEventId(eventId);
        request.setSeatsBooked(1);

        mockMvc.perform(post("/api/booking")
                        .header("Authorization", "Bearer " + attendeeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(11)
    void testCancelBookingRestoresSeatsAndStatus() throws Exception {
        mockMvc.perform(patch("/api/booking/" + bookingId)
                        .header("Authorization", "Bearer " + attendeeToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        mockMvc.perform(get("/api/event/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableSeats").value(3))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    @Order(12)
    void testGetEventsBySearch() throws Exception {
        mockMvc.perform(get("/api/event/search")
                        .param("searchTerm", "Flow Test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(13)
    void testFilterEventsByPriceRange() throws Exception {
        mockMvc.perform(get("/api/event/filter/price")
                        .param("minPrice", "10")
                        .param("maxPrice", "100")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(14)
    void testOrganizerCannotUpdateOthersEvent() throws Exception {
        UserRegisterRequest req = new UserRegisterRequest();
        req.setUsername("intruder_organizer");
        req.setEmail("intruder@test.com");
        req.setPassword("password123");
        mockMvc.perform(post("/api/auth/register-organizer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        String intruderToken = login("intruder@test.com", "password123");

        EventRequest updateRequest = new EventRequest();
        updateRequest.setTitle("Hijacked Title");
        updateRequest.setStartDateTime(LocalDateTime.now().plusDays(15));
        updateRequest.setPrice(1.0f);
        updateRequest.setTotalSeats(5);
        updateRequest.setVenueId(venueId);

        mockMvc.perform(put("/api/event/" + eventId)
                        .header("Authorization", "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }
}