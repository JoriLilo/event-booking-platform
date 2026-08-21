package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.EventRequest;
import com.example.EventBookingPlatform.dto.EventResponse;
import com.example.EventBookingPlatform.dto.StatusUpdateRequest;
import com.example.EventBookingPlatform.entity.Status;
import com.example.EventBookingPlatform.service.EventService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/event")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> addEvent(@Valid @RequestBody EventRequest eventRequest, Authentication authentication) {

        String organizerUsername = authentication.getName();

        return new ResponseEntity<>(eventService.createEvent(eventRequest, organizerUsername), HttpStatus.CREATED);
    }

    @PatchMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> updateEventStatus(
            @PathVariable Long eventId,
            @Valid @RequestBody StatusUpdateRequest statusRequest,
            Authentication authentication) {

        String organizerUsername = authentication.getName();

        return new ResponseEntity<>(
                eventService.updateEventStatus(eventId, Status.valueOf(statusRequest.getStatus()), organizerUsername),
                HttpStatus.OK
        );
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {

        return new ResponseEntity<>(eventService.getAllEvents(),HttpStatus.OK);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> searchEventById(@PathVariable Long eventId) {

        return new ResponseEntity<>(
                eventService.getEventById(eventId),
                HttpStatus.OK
        );
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequest eventRequest,
            Authentication authentication) {

        String organizerUsername = authentication.getName();

        return new ResponseEntity<>(eventService.updateEvent(eventId,eventRequest,organizerUsername), HttpStatus.OK);
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId, Authentication authentication) {

        String organizerUsername = authentication.getName();

        eventService.deleteEvent(eventId, organizerUsername);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/searchByOrganizer/{organizer}")
    public ResponseEntity<List<EventResponse>> searchAllEventsByOrganizer(@PathVariable String organizer) {

        return new ResponseEntity<>(eventService.getEventsByOrganizer(organizer), HttpStatus.OK);
    }

    // SEARCH AND FILTER ENDPOINTS

    @GetMapping("/search")
    public ResponseEntity<Page<EventResponse>> searchEvents(
            @RequestParam String searchTerm,
            Pageable pageable) {

        return new ResponseEntity<>(eventService.searchEvents(searchTerm, pageable), HttpStatus.OK);
    }

    @GetMapping("/filter/date")
    public ResponseEntity<Page<EventResponse>> filterByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            Pageable pageable) {

        return new ResponseEntity<>(
                eventService.filterByDateRange(startDate, endDate, pageable),
                HttpStatus.OK
        );
    }

    @GetMapping("/filter/price")
    public ResponseEntity<Page<EventResponse>> filterByPriceRange(
            @RequestParam float minPrice,
            @RequestParam float maxPrice,
            Pageable pageable) {

        return new ResponseEntity<>(
                eventService.filterByPriceRange(minPrice, maxPrice, pageable),
                HttpStatus.OK
        );
    }

    @GetMapping("/filter/category/{categoryId}")
    public ResponseEntity<Page<EventResponse>> filterByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {

        return new ResponseEntity<>(
                eventService.filterByCategory(categoryId, pageable),
                HttpStatus.OK
        );
    }

    @GetMapping("/filter/advanced")
    public ResponseEntity<Page<EventResponse>> searchWithFilters(
            @RequestParam String searchTerm,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam float minPrice,
            @RequestParam float maxPrice,
            Pageable pageable) {

        return new ResponseEntity<>(
                eventService.searchWithFilters(searchTerm, startDate, endDate, minPrice, maxPrice, pageable),
                HttpStatus.OK
        );
    }

    @GetMapping("/upcoming")
    public ResponseEntity<Page<EventResponse>> getUpcomingEvents(Pageable pageable) {

        return new ResponseEntity<>(eventService.getUpcomingEvents(pageable), HttpStatus.OK);
    }

    @GetMapping("/venue/{venueId}")
    public ResponseEntity<Page<EventResponse>> getEventsByVenue(
            @PathVariable Long venueId,
            Pageable pageable) {

        return new ResponseEntity<>(eventService.getEventsByVenue(venueId, pageable), HttpStatus.OK);
    }
}