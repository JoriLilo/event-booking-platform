package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.BookingRequest;
import com.example.EventBookingPlatform.dto.BookingResponse;
import com.example.EventBookingPlatform.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking")
@Tag(name = "Bookings", description = "Event booking management and seat reservations")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ATTENDEE')")
    @Operation(
            summary = "Create Booking",
            description = "Attendee books seats for an event. Seats are decremented and event status is updated if sold out."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Booking created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request, insufficient seats, or event sold out"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions (not an attendee)"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody BookingRequest bookingRequest,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return new ResponseEntity<>(bookingService.createBooking(bookingRequest, userEmail), HttpStatus.CREATED);
    }

    @PatchMapping("/{bookingId}")
    @PreAuthorize("hasRole('ATTENDEE')")
    @Operation(
            summary = "Cancel Booking",
            description = "Attendee cancels their booking. Seats are restored and waitlist users are promoted if available."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking cancelled successfully"),
            @ApiResponse(responseCode = "403", description = "Not your booking"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<BookingResponse> cancelBooking(
            @PathVariable Long bookingId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return new ResponseEntity<>(bookingService.cancelBooking(bookingId, userEmail), HttpStatus.OK);
    }

    @GetMapping("/{bookingId}")
    @Operation(
            summary = "Get Booking Details",
            description = "Retrieve details of a specific booking"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long bookingId) {
        return new ResponseEntity<>(bookingService.getBookingById(bookingId), HttpStatus.OK);
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('ATTENDEE')")
    @Operation(
            summary = "Get User's Bookings",
            description = "Retrieve all bookings made by the authenticated attendee"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(Authentication authentication) {
        String userEmail = authentication.getName();
        return new ResponseEntity<>(bookingService.getBookingsByUser(userEmail), HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}")
    @Operation(
            summary = "Get Event's Bookings",
            description = "Retrieve all bookings for a specific event"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<BookingResponse>> getBookingsByEvent(@PathVariable Long eventId) {
        return new ResponseEntity<>(bookingService.getBookingsByEvent(eventId), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get All Bookings",
            description = "Admin retrieves all bookings in the system. Requires ADMIN role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All bookings retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions (not an admin)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return new ResponseEntity<>(bookingService.geAllBookings(), HttpStatus.OK);
    }
}