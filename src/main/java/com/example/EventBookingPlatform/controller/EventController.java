package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.EventRequest;
import com.example.EventBookingPlatform.dto.EventResponse;
import com.example.EventBookingPlatform.entity.Status;
import com.example.EventBookingPlatform.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<EventResponse> addEvent(@RequestBody EventRequest eventRequest, Authentication authentication) {

        String organizerEmail = authentication.getName();

        return new ResponseEntity<>(eventService.createEvent(eventRequest, organizerEmail), HttpStatus.CREATED);
    }

    @PatchMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<EventResponse> updateEventStatus(@PathVariable Long eventId, @RequestBody String status, Authentication authentication) {

        String organizerEmail = authentication.getName();

        return new ResponseEntity<>(eventService.updateEventStatus(eventId, Status.valueOf(status), organizerEmail), HttpStatus.OK);
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
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long eventId, @RequestBody EventRequest eventRequest, Authentication authentication) {

        String organizerEmail = authentication.getName();

        return new ResponseEntity<>(eventService.updateEvent(eventId,eventRequest,organizerEmail), HttpStatus.OK);
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId, Authentication authentication) {

        String organizerEmail = authentication.getName();

        eventService.deleteEvent(eventId, organizerEmail);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/searchByOrganizer/{organizer}")
    public ResponseEntity<List<EventResponse>> searchAllEventsByOrganizer(@PathVariable String organizer) {

        return new ResponseEntity<>(eventService.getEventsByOrganizer(organizer), HttpStatus.OK);
    }
}