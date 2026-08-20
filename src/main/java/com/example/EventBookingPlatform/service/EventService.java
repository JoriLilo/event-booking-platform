package com.example.EventBookingPlatform.service;

import com.example.EventBookingPlatform.dto.EventRequest;
import com.example.EventBookingPlatform.dto.EventResponse;
import com.example.EventBookingPlatform.entity.Event;
import com.example.EventBookingPlatform.entity.Status;
import com.example.EventBookingPlatform.entity.User;
import com.example.EventBookingPlatform.exception.EventNotFoundException;
import com.example.EventBookingPlatform.exception.UserNotFoundException;
import com.example.EventBookingPlatform.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public EventService(EventRepository eventRepository, VenueRepository venueRepository, CategoryRepository categoryRepository, UserRepository userRepository, ReviewRepository reviewRepository) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
    }

    public EventResponse createEvent(EventRequest eventRequest, String organizerUsername) {

        if (eventRequest.getTitle() == null || eventRequest.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (eventRequest.getStartDateTime() == null) {
            throw new IllegalArgumentException("Start date/time is required");
        }
        if (eventRequest.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Start date/time cannot be in the past");
        }
        if (eventRequest.getEndDateTime() != null && eventRequest.getEndDateTime().isBefore(eventRequest.getStartDateTime())) {
            throw new IllegalArgumentException("End date/time cannot be before start date/time");
        }
        if (eventRequest.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (eventRequest.getTotalSeats() <= 0) {
            throw new IllegalArgumentException("Total seats must be greater than zero");
        }
        if (eventRequest.getVenueId() == null) {
            throw new IllegalArgumentException("Venue is required");
        }


        Event event = new Event();
        event.setTitle(eventRequest.getTitle());
        event.setDescription(eventRequest.getDescription());
        event.setStartDateTime(eventRequest.getStartDateTime());
        event.setEndDateTime(eventRequest.getEndDateTime());
        event.setPrice(eventRequest.getPrice());
        event.setTotalSeats(eventRequest.getTotalSeats());
        event.setVenue(venueRepository.findById(eventRequest.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue not found")));
        event.setCategories(categoryRepository.findAllById(eventRequest.getCategoryIds()));
        event.setUser(userRepository.findByUsername(organizerUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found")));
        event.setStatus(Status.AVAILABLE);

        eventRepository.save(event);

        EventResponse response = eventToResponse(event);

        return response;
    }


    public EventResponse updateEventStatus(Long eventId, Status status, String organizerEmail) {

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        User organizer = userRepository.findByEmail(organizerEmail)
                .orElseThrow(() -> new UserNotFoundException("Organizer not found"));

        if (!event.getUser().getId().equals(organizer.getId())) {
            throw new AccessDeniedException(
                    "You are not allowed to update this event"
            );
        }

        event.setStatus(status);
        eventRepository.save(event);

        EventResponse response = eventToResponse(event);

        return response;
    }

    public List<EventResponse> getAllEvents() {
        List<Event> events = eventRepository.findAll();
        List<EventResponse> responses = new ArrayList<>();
        for (Event event : events) {
            EventResponse response = eventToResponse(event);
            responses.add(response);
        }
        return responses;
    }

    public EventResponse getEventById(Long eventId) {

        Event event= eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        EventResponse response = eventToResponse(event);
        return response;

    }


    public EventResponse updateEvent(Long eventId, EventRequest eventRequest, String organizerUsername){
        Event event = eventRepository.findById(eventId)
                .orElseThrow(()-> new EventNotFoundException("Event not found"));

        if (!event.getUser().getUsername().equals(organizerUsername)) {
            throw new IllegalArgumentException("You are not the organizer of this event");
        }

        event.setTitle(eventRequest.getTitle());
        event.setDescription(eventRequest.getDescription());
        event.setStartDateTime(eventRequest.getStartDateTime());
        event.setEndDateTime(eventRequest.getEndDateTime());
        event.setPrice(eventRequest.getPrice());
        event.setTotalSeats(eventRequest.getTotalSeats());
        event.setVenue(venueRepository.findById(eventRequest.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue not found")));
        event.setCategories(categoryRepository.findAllById(eventRequest.getCategoryIds()));

        eventRepository.save(event);

        EventResponse response = eventToResponse(event);
        return response;

    }


    public void deleteEvent(Long eventId, String organizerUsername) {
        User organizer = userRepository.findByUsername(organizerUsername)
                .orElseThrow(() -> new UserNotFoundException(" User not found "));

        Event event = eventRepository.findByIdAndUser(eventId, organizer)
                .orElseThrow(() -> new IllegalArgumentException(" Event not found "));

        eventRepository.delete(event);
    }


    public List<EventResponse> getEventsByOrganizer(String organizerUsername){

        User organizer = userRepository.findByUsername(organizerUsername)
                .orElseThrow(() -> new UserNotFoundException(" User not found "));
        List<Event> events = eventRepository.findByUser(organizer);
        List<EventResponse> responses = new ArrayList<>();

        for (Event event : events) {
            EventResponse response = eventToResponse(event);
            responses.add(response);
        }
        return responses;
    }




    private EventResponse eventToResponse(Event event) {

        EventResponse response = new EventResponse();

        response.setId(event.getId());
        response.setTitle(event.getTitle());
        response.setDescription(event.getDescription());
        response.setStartDateTime(event.getStartDateTime());
        response.setEndDateTime(event.getEndDateTime());
        response.setPrice(event.getPrice());
        response.setTotalSeats(event.getTotalSeats());
        response.setCategoryNames(
                event.getCategories().stream()
                        .map(category -> category.getCategoryName())
                        .collect(Collectors.toList())
        );
        response.setAverageRating(reviewRepository.getAverageRatingForEvent(event.getId()));
        response.setVenueName(event.getVenue().getName());
        response.setStatus(event.getStatus().toString());
        response.setOrganizerUsername(event.getUser().getUsername());
        return response;

    }
}
