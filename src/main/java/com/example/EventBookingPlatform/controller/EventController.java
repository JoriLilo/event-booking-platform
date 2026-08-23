package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.EventRequest;
import com.example.EventBookingPlatform.dto.EventResponse;
import com.example.EventBookingPlatform.dto.StatusUpdateRequest;
import com.example.EventBookingPlatform.entity.Status;
import com.example.EventBookingPlatform.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Events", description = "Event creation, management, search, and filtering")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(
            summary = "Create Event",
            description = "Organizer creates a new event with venue and categories. Requires ORGANIZER role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation error"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions (not an organizer)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<EventResponse> addEvent(@Valid @RequestBody EventRequest eventRequest, Authentication authentication) {
        String organizerEmail = authentication.getName();
        return new ResponseEntity<>(eventService.createEvent(eventRequest, organizerEmail), HttpStatus.CREATED);
    }

    @PatchMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(
            summary = "Update Event Status",
            description = "Organizer updates event status (AVAILABLE/SOLD_OUT). Only event creator can update."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event status updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid status value"),
            @ApiResponse(responseCode = "403", description = "Not the event organizer"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<EventResponse> updateEventStatus(
            @PathVariable Long eventId,
            @Valid @RequestBody StatusUpdateRequest statusRequest,
            Authentication authentication) {
        String organizerEmail = authentication.getName();
        return new ResponseEntity<>(
                eventService.updateEventStatus(eventId, Status.valueOf(statusRequest.getStatus()), organizerEmail),
                HttpStatus.OK
        );
    }

    @GetMapping
    @Operation(
            summary = "Get All Events",
            description = "Retrieve all events (public, no authentication required)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
    })
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        return new ResponseEntity<>(eventService.getAllEvents(), HttpStatus.OK);
    }

    @GetMapping("/{eventId}")
    @Operation(
            summary = "Get Event Details",
            description = "Retrieve detailed information for a specific event including average rating"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<EventResponse> searchEventById(@PathVariable Long eventId) {
        return new ResponseEntity<>(eventService.getEventById(eventId), HttpStatus.OK);
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(
            summary = "Update Event",
            description = "Organizer updates event details (title, price, seats, etc). Only event creator can update."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation error"),
            @ApiResponse(responseCode = "403", description = "Not the event organizer"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventRequest eventRequest,
            Authentication authentication) {
        String organizerEmail = authentication.getName();
        return new ResponseEntity<>(eventService.updateEvent(eventId, eventRequest, organizerEmail), HttpStatus.OK);
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('ORGANIZER')")
    @Operation(
            summary = "Delete Event",
            description = "Organizer deletes their event. Only event creator can delete."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Event deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Not the event organizer"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId, Authentication authentication) {
        String organizerEmail = authentication.getName();
        eventService.deleteEvent(eventId, organizerEmail);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search Events",
            description = "Search events by title or description. Supports pagination and sorting (DERIVED QUERY)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    })
    public ResponseEntity<Page<EventResponse>> searchEvents(
            @RequestParam String searchTerm,
            Pageable pageable) {
        return new ResponseEntity<>(eventService.searchEvents(searchTerm, pageable), HttpStatus.OK);
    }

    @GetMapping("/filter/date")
    @Operation(
            summary = "Filter Events by Date Range",
            description = "Filter events by start and end date (JPQL QUERY)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filtered events retrieved successfully")
    })
    public ResponseEntity<Page<EventResponse>> filterByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            Pageable pageable) {
        return new ResponseEntity<>(eventService.filterByDateRange(startDate, endDate, pageable), HttpStatus.OK);
    }

    @GetMapping("/filter/price")
    @Operation(
            summary = "Filter Events by Price Range",
            description = "Filter events by minimum and maximum price (NATIVE QUERY)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filtered events retrieved successfully")
    })
    public ResponseEntity<Page<EventResponse>> filterByPriceRange(
            @RequestParam float minPrice,
            @RequestParam float maxPrice,
            Pageable pageable) {
        return new ResponseEntity<>(eventService.filterByPriceRange(minPrice, maxPrice, pageable), HttpStatus.OK);
    }

    @GetMapping("/filter/category/{categoryId}")
    @Operation(
            summary = "Filter Events by Category",
            description = "Filter events by category ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Filtered events retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Page<EventResponse>> filterByCategory(
            @PathVariable Long categoryId,
            Pageable pageable) {
        return new ResponseEntity<>(eventService.filterByCategory(categoryId, pageable), HttpStatus.OK);
    }

    @GetMapping("/upcoming")
    @Operation(
            summary = "Get Upcoming Events",
            description = "Retrieve events that are scheduled for future dates"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Upcoming events retrieved successfully")
    })
    public ResponseEntity<Page<EventResponse>> getUpcomingEvents(Pageable pageable) {
        return new ResponseEntity<>(eventService.getUpcomingEvents(pageable), HttpStatus.OK);
    }

    @GetMapping("/venue/{venueId}")
    @Operation(
            summary = "Get Events by Venue",
            description = "Retrieve all events at a specific venue"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Venue not found")
    })
    public ResponseEntity<Page<EventResponse>> getEventsByVenue(
            @PathVariable Long venueId,
            Pageable pageable) {
        return new ResponseEntity<>(eventService.getEventsByVenue(venueId, pageable), HttpStatus.OK);
    }
}