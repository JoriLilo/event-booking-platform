package com.example.EventBookingPlatform.service;

import com.example.EventBookingPlatform.dto.WaitlistRequest;
import com.example.EventBookingPlatform.dto.WaitlistResponse;
import com.example.EventBookingPlatform.entity.*;
import com.example.EventBookingPlatform.exception.EventNotFoundException;
import com.example.EventBookingPlatform.exception.UserNotFoundException;
import com.example.EventBookingPlatform.repository.BookingRepository;
import com.example.EventBookingPlatform.repository.EventRepository;
import com.example.EventBookingPlatform.repository.UserRepository;
import com.example.EventBookingPlatform.repository.WaitlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitlistServiceTest {

    @Mock
    private WaitlistRepository waitlistRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private WaitlistService waitlistService;

    private WaitlistRequest waitlistRequest;
    private User user;
    private Event event;
    private Waitlist waitlist;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setRole(Role.ATTENDEE);

        Venue venue = new Venue();
        venue.setId(1L);
        venue.setName("Convention Center");

        event = new Event();
        event.setId(1L);
        event.setTitle("Tech Conference");
        event.setStatus(Status.SOLD_OUT);
        event.setAvailableSeats(0);
        event.setVenue(venue);

        waitlistRequest = new WaitlistRequest();
        waitlistRequest.setEventId(1L);

        waitlist = new Waitlist();
        waitlist.setId(1L);
        waitlist.setUser(user);
        waitlist.setEvent(event);
        waitlist.setPosition(1);
        waitlist.setStatus(WaitlistStatus.WAITING);
        waitlist.setAddedAt(LocalDateTime.now());
    }

    @Test
    void testAddToWaitlistSuccess() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(bookingRepository.existsByUserAndEventAndStatus(user, event, BookingStatus.CONFIRMED)).thenReturn(false);
        when(waitlistRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());
        when(waitlistRepository.findWaitingByEventOrderByPosition(1L)).thenReturn(new ArrayList<>());
        when(waitlistRepository.save(any(Waitlist.class))).thenReturn(waitlist);

        WaitlistResponse response = waitlistService.addToWaitlist(waitlistRequest, "john@example.com");

        assertNotNull(response);
        assertEquals("Tech Conference", response.getEventTitle());
        assertEquals(1, response.getPosition());
        assertEquals("WAITING", response.getStatus());
    }

    @Test
    void testAddToWaitlistAlreadyBooked() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(bookingRepository.existsByUserAndEventAndStatus(user, event, BookingStatus.CONFIRMED)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                waitlistService.addToWaitlist(waitlistRequest, "john@example.com"));
    }

    @Test
    void testAddToWaitlistAlreadyWaiting() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(bookingRepository.existsByUserAndEventAndStatus(user, event, BookingStatus.CONFIRMED)).thenReturn(false);
        when(waitlistRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(waitlist));

        assertThrows(IllegalArgumentException.class, () ->
                waitlistService.addToWaitlist(waitlistRequest, "john@example.com"));
    }

    @Test
    void testRemoveFromWaitlist() {
        when(waitlistRepository.findById(1L)).thenReturn(Optional.of(waitlist));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        waitlistService.removeFromWaitlist(1L, "john@example.com");

        verify(waitlistRepository, times(1)).delete(waitlist);
    }

    @Test
    void testGetWaitlistById() {
        when(waitlistRepository.findById(1L)).thenReturn(Optional.of(waitlist));

        WaitlistResponse response = waitlistService.getWaitlistById(1L);

        assertNotNull(response);
        assertEquals(1, response.getPosition());
        assertEquals("Tech Conference", response.getEventTitle());
    }

    @Test
    void testGetWaitlistPosition() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(waitlistRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(waitlist));

        int position = waitlistService.getWaitlistPosition(1L, "john@example.com");

        assertEquals(1, position);
    }

    @Test
    void testGetWaitlistPositionNotOnWaitlist() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(waitlistRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());

        int position = waitlistService.getWaitlistPosition(1L, "john@example.com");

        assertEquals(-1, position);
    }

    @Test
    void testPromoteFromWaitlist() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(waitlistRepository.findWaitingByEventOrderByPosition(1L)).thenReturn(List.of(waitlist));
        event.setAvailableSeats(1);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(bookingRepository.save(any(Booking.class))).thenReturn(new Booking());
        when(waitlistRepository.save(any(Waitlist.class))).thenReturn(waitlist);

        waitlistService.promoteFromWaitlist(1L);

        assertEquals(WaitlistStatus.PROMOTED, waitlist.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void testGetWaitlistByEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(waitlistRepository.findByEventOrderByPosition(event)).thenReturn(List.of(waitlist));

        List<WaitlistResponse> responses = waitlistService.getWaitlistByEvent(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }

    @Test
    void testGetWaitlistByUser() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(waitlistRepository.findByUserAndStatus(user, WaitlistStatus.WAITING)).thenReturn(List.of(waitlist));

        List<WaitlistResponse> responses = waitlistService.getWaitlistByUser("john@example.com");

        assertNotNull(responses);
        assertEquals(1, responses.size());
    }
}