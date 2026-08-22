package com.example.EventBookingPlatform.service;

import com.example.EventBookingPlatform.dto.BookingRequest;
import com.example.EventBookingPlatform.dto.BookingResponse;
import com.example.EventBookingPlatform.entity.*;
import com.example.EventBookingPlatform.exception.BookingNotFoundException;
import com.example.EventBookingPlatform.exception.EventNotFoundException;
import com.example.EventBookingPlatform.exception.UserNotFoundException;
import com.example.EventBookingPlatform.repository.BookingRepository;
import com.example.EventBookingPlatform.repository.EventRepository;
import com.example.EventBookingPlatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WaitlistService waitlistService;

    @InjectMocks
    private BookingService bookingService;

    private BookingRequest bookingRequest;
    private User user;
    private Event event;
    private Booking booking;

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
        venue.setCapacity(500);

        User organizer = new User();
        organizer.setId(2L);
        organizer.setUsername("organizer");
        organizer.setEmail("organizer@example.com");

        event = new Event();
        event.setId(1L);
        event.setTitle("Tech Conference");
        event.setStartDateTime(LocalDateTime.now().plusDays(10));
        event.setEndDateTime(LocalDateTime.now().plusDays(10).plusHours(8));
        event.setPrice(99.99f);
        event.setTotalSeats(100);
        event.setAvailableSeats(50);
        event.setStatus(Status.AVAILABLE);
        event.setVenue(venue);
        event.setUser(organizer);
        event.setBookings(new ArrayList<>());

        bookingRequest = new BookingRequest();
        bookingRequest.setEventId(1L);
        bookingRequest.setSeatsBooked(2);

        booking = new Booking();
        booking.setId(1L);
        booking.setSeatsBooked(2);
        booking.setEvent(event);
        booking.setUser(user);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDateTime.now());
    }

    @Test
    void testCreateBookingSuccess() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponse response = bookingService.createBooking(bookingRequest, "john@example.com");

        assertNotNull(response);
        assertEquals("Tech Conference", response.getEventTitle());
        assertEquals(2, response.getSeatsBooked());
        assertEquals("CONFIRMED", response.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void testCreateBookingEventNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () ->
                bookingService.createBooking(bookingRequest, "john@example.com"));
    }

    @Test
    void testCreateBookingUserNotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                bookingService.createBooking(bookingRequest, "john@example.com"));
    }

    @Test
    void testCreateBookingInsufficientSeats() {
        bookingRequest.setSeatsBooked(100);
        event.setAvailableSeats(50);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBooking(bookingRequest, "john@example.com"));
    }

    @Test
    void testCreateBookingEventSoldOut() {
        event.setStatus(Status.SOLD_OUT);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBooking(bookingRequest, "john@example.com"));
    }

    @Test
    void testCancelBookingSuccess() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        BookingResponse response = bookingService.cancelBooking(1L, "john@example.com");

        assertNotNull(response);
        assertEquals("CANCELLED", response.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void testCancelBookingNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () ->
                bookingService.cancelBooking(1L, "john@example.com"));
    }

    @Test
    void testCancelBookingUnauthorized() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setEmail("other@example.com");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(otherUser));

        assertThrows(BookingNotFoundException.class, () ->
                bookingService.cancelBooking(1L, "other@example.com"));
    }

    @Test
    void testCancelBookingSoldOutEventRestored() {
        event.setStatus(Status.SOLD_OUT);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        bookingService.cancelBooking(1L, "john@example.com");

        assertEquals(Status.AVAILABLE, event.getStatus());
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void testGetBookingById() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.getBookingById(1L);

        assertNotNull(response);
        assertEquals("Tech Conference", response.getEventTitle());
    }

    @Test
    void testGetBookingByIdNotFound() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () ->
                bookingService.getBookingById(1L));
    }

    @Test
    void testZeroSeatsBooking() {
        bookingRequest.setSeatsBooked(0);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () ->
                bookingService.createBooking(bookingRequest, "john@example.com"));
    }

    @Test
    void testEventSeatsDecrementedOnBooking() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        int availableBeforeBooking = event.getAvailableSeats();
        bookingService.createBooking(bookingRequest, "john@example.com");

        assertEquals(availableBeforeBooking - 2, event.getAvailableSeats());
    }
}