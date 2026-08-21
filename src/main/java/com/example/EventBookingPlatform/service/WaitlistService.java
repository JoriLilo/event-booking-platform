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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public WaitlistService(WaitlistRepository waitlistRepository, EventRepository eventRepository, UserRepository userRepository, BookingRepository bookingRepository) {

        this.waitlistRepository = waitlistRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public WaitlistResponse addToWaitlist(WaitlistRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new EventNotFoundException("Event not found"));


        if (bookingRepository.existsByUserAndEventAndStatus(user, event, BookingStatus.CONFIRMED)) {
            throw new IllegalArgumentException("You already have a confirmed booking for this event");
        }


        Optional<Waitlist> existingWaitlist = waitlistRepository.findByUserAndEvent(user, event);
        if (existingWaitlist.isPresent() && existingWaitlist.get().getStatus() == WaitlistStatus.WAITING) {
            throw new IllegalArgumentException("You are already on the waitlist for this event");
        }


        List<Waitlist> waitingList = waitlistRepository.findWaitingByEventOrderByPosition(event.getId());
        int nextPosition = waitingList.size() + 1;

        Waitlist waitlist = new Waitlist();
        waitlist.setUser(user);
        waitlist.setEvent(event);
        waitlist.setPosition(nextPosition);
        waitlist.setAddedAt(LocalDateTime.now());
        waitlist.setStatus(WaitlistStatus.WAITING);

        waitlistRepository.save(waitlist);

        return waitlistToResponse(waitlist);
    }

    @Transactional
    public void removeFromWaitlist(Long waitlistId, String userEmail) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!waitlist.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You cannot remove this waitlist entry");
        }

        waitlistRepository.delete(waitlist);
    }

    @Transactional
    public void promoteFromWaitlist(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        if (event.getAvailableSeats() <= 0) {
            return;
        }

        List<Waitlist> waitingList = waitlistRepository.findWaitingByEventOrderByPosition(eventId);

        for (Waitlist waitlist : waitingList) {
            if (event.getAvailableSeats() <= 0) {
                break;
            }

            // Promote user by creating booking
            Booking booking = new Booking();
            booking.setUser(waitlist.getUser());
            booking.setEvent(event);
            booking.setSeatsBooked(1);
            booking.setBookingDate(LocalDateTime.now());
            booking.setStatus(BookingStatus.CONFIRMED);

            event.setAvailableSeats(event.getAvailableSeats() - 1);
            if (event.getAvailableSeats() == 0) {
                event.setStatus(Status.SOLD_OUT);
            }

            eventRepository.save(event);
            bookingRepository.save(booking);

            waitlist.setStatus(WaitlistStatus.PROMOTED);
            waitlistRepository.save(waitlist);
        }
    }

    public WaitlistResponse getWaitlistById(Long waitlistId) {
        Waitlist waitlist = waitlistRepository.findById(waitlistId)
                .orElseThrow(() -> new IllegalArgumentException("Waitlist entry not found"));

        return waitlistToResponse(waitlist);
    }

    public List<WaitlistResponse> getWaitlistByEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        List<Waitlist> waitlists = waitlistRepository.findByEventOrderByPosition(event);
        List<WaitlistResponse> responses = new ArrayList<>();

        for (Waitlist waitlist : waitlists) {
            responses.add(waitlistToResponse(waitlist));
        }

        return responses;
    }

    public List<WaitlistResponse> getWaitlistByUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Waitlist> waitlists = waitlistRepository.findByUserAndStatus(user, WaitlistStatus.WAITING);
        List<WaitlistResponse> responses = new ArrayList<>();

        for (Waitlist waitlist : waitlists) {
            responses.add(waitlistToResponse(waitlist));
        }

        return responses;
    }

    public int getWaitlistPosition(Long eventId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        Optional<Waitlist> waitlist = waitlistRepository.findByUserAndEvent(user, event);

        return waitlist.map(Waitlist::getPosition).orElse(-1);
    }

    private WaitlistResponse waitlistToResponse(Waitlist waitlist) {
        WaitlistResponse response = new WaitlistResponse();
        response.setId(waitlist.getId());
        response.setEventTitle(waitlist.getEvent().getTitle());
        response.setUserUsername(waitlist.getUser().getUsername());
        response.setPosition(waitlist.getPosition());
        response.setStatus(waitlist.getStatus().toString());
        response.setAddedAt(waitlist.getAddedAt());
        return response;
    }
}