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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final WaitlistService waitlistService;
    private final int cancellationWindowHours;

    public BookingService(BookingRepository bookingRepository, EventRepository eventRepository, UserRepository userRepository, WaitlistService waitlistService,
                          @Value("${booking.cancellation.hours-before:2}") int cancellationWindowHours) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.waitlistService = waitlistService;
        this.cancellationWindowHours = cancellationWindowHours;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest bookingRequest, String userEmail) {

        Event event = eventRepository.findById(bookingRequest.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (bookingRequest.getSeatsBooked() <= 0) {
            throw new IllegalArgumentException("Seats booked must be greater than zero");
        }

        if (event.getStatus() == Status.SOLD_OUT) {
            throw new IllegalArgumentException("Event is sold out");
        }

        if (bookingRequest.getSeatsBooked() > event.getAvailableSeats()) {
            throw new IllegalArgumentException("Not enough seats available");
        }

        event.setAvailableSeats(event.getAvailableSeats() - bookingRequest.getSeatsBooked());

        // Set status to SOLD_OUT if no seats remain
        if (event.getAvailableSeats() == 0) {
            event.setStatus(Status.SOLD_OUT);
        }

        eventRepository.save(event);

        Booking booking = new Booking();
        booking.setSeatsBooked(bookingRequest.getSeatsBooked());
        booking.setBookingDate(LocalDateTime.now());
        booking.setEvent(event);
        booking.setUser(user);
        booking.setStatus(BookingStatus.CONFIRMED);

        bookingRepository.save(booking);

        return bookingToResponse(booking);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId, String userEmail) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new BookingNotFoundException("Booking not found");
        }

        Event event = booking.getEvent();

        // Cancellation policy: cannot cancel within X hours of event start
        LocalDateTime now = LocalDateTime.now();
        if (event.getStartDateTime().isBefore(now.plusHours(cancellationWindowHours))) {
            throw new IllegalArgumentException(
                    "Cannot cancel booking within " + cancellationWindowHours + " hours of event start");
        }

        event.setAvailableSeats(event.getAvailableSeats() + booking.getSeatsBooked());

        if (event.getStatus() == Status.SOLD_OUT) {
            event.setStatus(Status.AVAILABLE);
        }

        eventRepository.save(event);

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Promote from waitlist if available seats opened up
        waitlistService.promoteFromWaitlist(event.getId());

        return bookingToResponse(booking);
    }

    public BookingResponse getBookingById(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        return bookingToResponse(booking);
    }

    public List<BookingResponse> getBookingsByUser(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Booking> bookings = user.getBookings();

        List<BookingResponse> bookingResponses = new ArrayList<>();

        for (Booking booking : bookings) {
            BookingResponse bookingResponse = bookingToResponse(booking);
            bookingResponses.add(bookingResponse);
        }

        return bookingResponses;
    }

    public List<BookingResponse> getBookingsByEvent(Long eventId) {

        List<Booking> bookings = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"))
                .getBookings();

        List<BookingResponse> bookingResponses = new ArrayList<>();

        for (Booking booking : bookings) {
            BookingResponse bookingResponse = bookingToResponse(booking);
            bookingResponses.add(bookingResponse);
        }

        return bookingResponses;
    }

    public List<BookingResponse> geAllBookings() {

        List<Booking> bookings = bookingRepository.findAll();

        List<BookingResponse> bookingResponses = new ArrayList<>();

        for (Booking booking : bookings) {
            BookingResponse bookingResponse = bookingToResponse(booking);
            bookingResponses.add(bookingResponse);
        }

        return bookingResponses;
    }

    @Transactional
    public BookingResponse adminCancelBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

        booking.setStatus(BookingStatus.CANCELLED);

        bookingRepository.save(booking);

        // Promote from waitlist
        waitlistService.promoteFromWaitlist(booking.getEvent().getId());

        return bookingToResponse(booking);
    }

    private BookingResponse bookingToResponse(Booking booking) {

        BookingResponse response = new BookingResponse();

        response.setId(booking.getId());
        response.setEventTitle(booking.getEvent().getTitle());
        response.setSeatsBooked(booking.getSeatsBooked());
        response.setBookingDate(booking.getBookingDate());
        response.setStatus(booking.getStatus().toString());
        response.setAttendeeUsername(booking.getUser().getUsername());

        return response;
    }
}