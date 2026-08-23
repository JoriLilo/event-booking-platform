package com.example.EventBookingPlatform.service;

import com.example.EventBookingPlatform.dto.EventRequest;
import com.example.EventBookingPlatform.dto.EventResponse;
import com.example.EventBookingPlatform.entity.*;
import com.example.EventBookingPlatform.exception.EventNotFoundException;
import com.example.EventBookingPlatform.exception.UserNotFoundException;
import com.example.EventBookingPlatform.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private EventService eventService;

    private EventRequest eventRequest;
    private User organizer;
    private Venue venue;
    private Event event;

    @BeforeEach
    void setUp() {
        organizer = new User();
        organizer.setId(1L);
        organizer.setUsername("john_organizer");
        organizer.setEmail("john@example.com");
        organizer.setRole(Role.ORGANIZER);

        venue = new Venue();
        venue.setId(1L);
        venue.setName("Convention Center");
        venue.setCapacity(500);

        eventRequest = new EventRequest();
        eventRequest.setTitle("Tech Conference 2024");
        eventRequest.setDescription("A great tech conference");
        eventRequest.setStartDateTime(LocalDateTime.now().plusDays(10));
        eventRequest.setEndDateTime(LocalDateTime.now().plusDays(10).plusHours(8));
        eventRequest.setPrice(99.99f);
        eventRequest.setTotalSeats(100);
        eventRequest.setVenueId(1L);
        eventRequest.setCategoryIds(new ArrayList<>());

        event = new Event();
        event.setId(1L);
        event.setTitle(eventRequest.getTitle());
        event.setDescription(eventRequest.getDescription());
        event.setStartDateTime(eventRequest.getStartDateTime());
        event.setEndDateTime(eventRequest.getEndDateTime());
        event.setPrice(eventRequest.getPrice());
        event.setTotalSeats(eventRequest.getTotalSeats());
        event.setAvailableSeats(eventRequest.getTotalSeats());
        event.setUser(organizer);
        event.setVenue(venue);
        event.setStatus(Status.AVAILABLE);
        event.setCategories(new ArrayList<>());
        event.setReviews(new ArrayList<>());
        event.setBookings(new ArrayList<>());
    }

    @Test
    void testCreateEventSuccess() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        // Fixed: Changed from findByUsername to findByEmail
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(organizer));
        when(categoryRepository.findAllById(any())).thenReturn(new ArrayList<>());

        // Fix: Set ID when event is saved
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event savedEvent = invocation.getArgument(0);
            savedEvent.setId(1L); // Set the ID here
            return savedEvent;
        });

        when(reviewRepository.getAverageRatingForEvent(1L)).thenReturn(null);

        // Fixed: Pass email instead of username
        EventResponse response = eventService.createEvent(eventRequest, "john@example.com");

        assertNotNull(response);
        assertEquals("Tech Conference 2024", response.getTitle());
        assertEquals(100, response.getAvailableSeats());
        assertEquals(100, response.getTotalSeats());
        assertEquals("AVAILABLE", response.getStatus());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testCreateEventWithNullTitle() {
        eventRequest.setTitle(null);
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(eventRequest, "john@example.com"));
    }

    @Test
    void testCreateEventWithPastDate() {
        eventRequest.setStartDateTime(LocalDateTime.now().minusDays(1));
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(eventRequest, "john@example.com"));
    }

    @Test
    void testCreateEventWithNegativePrice() {
        eventRequest.setPrice(-50);
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(eventRequest, "john@example.com"));
    }

    @Test
    void testGetEventById() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(reviewRepository.getAverageRatingForEvent(1L)).thenReturn(4.5);

        EventResponse response = eventService.getEventById(1L);

        assertNotNull(response);
        assertEquals("Tech Conference 2024", response.getTitle());
        assertEquals(4.5, response.getAverageRating());
    }

    @Test
    void testGetEventByIdNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EventNotFoundException.class, () -> eventService.getEventById(1L));
    }

    @Test
    void testSearchEvents() {
        Page<Event> eventPage = new PageImpl<>(List.of(event));
        Pageable pageable = PageRequest.of(0, 10);
        when(eventRepository.searchByTitleOrDescription("Tech", pageable)).thenReturn(eventPage);
        when(reviewRepository.getAverageRatingForEvent(1L)).thenReturn(null);

        Page<EventResponse> responses = eventService.searchEvents("Tech", pageable);

        assertNotNull(responses);
        assertEquals(1, responses.getTotalElements());
    }

    @Test
    void testFilterByPriceRange() {
        Page<Event> eventPage = new PageImpl<>(List.of(event));
        Pageable pageable = PageRequest.of(0, 10);
        when(eventRepository.findByPriceRange(50, 150, pageable)).thenReturn(eventPage);
        when(reviewRepository.getAverageRatingForEvent(1L)).thenReturn(null);

        Page<EventResponse> responses = eventService.filterByPriceRange(50, 150, pageable);

        assertNotNull(responses);
        assertEquals(1, responses.getTotalElements());
    }
}