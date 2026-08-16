package com.example.EventBookingPlatform.controller;


import com.example.EventBookingPlatform.dto.EventRequest;
import com.example.EventBookingPlatform.dto.EventResponse;
import com.example.EventBookingPlatform.entity.Event;
import com.example.EventBookingPlatform.entity.Status;
import com.example.EventBookingPlatform.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/event")
public class EventController {

    private final  EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/{organizerUsername}")
    public ResponseEntity<EventResponse> addEvent(@RequestBody EventRequest eventRequest, @PathVariable String organizerUsername) {

        return new ResponseEntity<>(eventService.createEvent(eventRequest, organizerUsername), HttpStatus.CREATED);
    }


    @PatchMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEventStatus(@PathVariable Long eventId ,@RequestBody String status) {
        return new ResponseEntity<>(eventService.updateEventStatus(eventId, Status.valueOf(status)),HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return new ResponseEntity<>(eventService.getAllEvents(), HttpStatus.OK);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> searchEventById(@PathVariable Long eventId) {
        return new ResponseEntity<>(eventService.getEventById(eventId), HttpStatus.OK);

    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(@PathVariable Long eventId, @RequestBody EventRequest eventRequest,  @RequestParam String organizerUsername) {
        return new ResponseEntity<>(eventService.updateEvent(eventId,eventRequest, organizerUsername), HttpStatus.OK);

    }

    @DeleteMapping("/{eventId}/{organizerUsername}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId, @PathVariable String organizerUsername) {
        eventService.deleteEvent(eventId, organizerUsername);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/searchByOrganizer/{organizer}")
    public ResponseEntity<List<EventResponse>> searchAllEventsByOrganizer(@PathVariable String organizer) {
        return new ResponseEntity<>(eventService.getEventsByOrganizer(organizer), HttpStatus.OK);
    }
}
