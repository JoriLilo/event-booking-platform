package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.WaitlistRequest;
import com.example.EventBookingPlatform.dto.WaitlistResponse;
import com.example.EventBookingPlatform.service.WaitlistService;
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
@RequestMapping("/api/waitlist")
@Tag(name = "Waitlist", description = "Event waitlist management for sold-out events")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ATTENDEE')")
    @Operation(
            summary = "Add to Waitlist",
            description = "Attendee joins the waitlist for a sold-out event. Automatically promoted when seats become available."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Added to waitlist successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or already on waitlist"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions (not an attendee)"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<WaitlistResponse> addToWaitlist(
            @Valid @RequestBody WaitlistRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return new ResponseEntity<>(waitlistService.addToWaitlist(request, userEmail), HttpStatus.CREATED);
    }

    @DeleteMapping("/{waitlistId}")
    @PreAuthorize("hasRole('ATTENDEE')")
    @Operation(
            summary = "Remove from Waitlist",
            description = "Attendee removes themselves from the waitlist"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Removed from waitlist successfully"),
            @ApiResponse(responseCode = "403", description = "Not your waitlist entry"),
            @ApiResponse(responseCode = "404", description = "Waitlist entry not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> removeFromWaitlist(
            @PathVariable Long waitlistId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        waitlistService.removeFromWaitlist(waitlistId, userEmail);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{waitlistId}")
    @Operation(
            summary = "Get Waitlist Entry",
            description = "Retrieve details of a specific waitlist entry"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Waitlist entry retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Waitlist entry not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<WaitlistResponse> getWaitlistById(@PathVariable Long waitlistId) {
        return new ResponseEntity<>(waitlistService.getWaitlistById(waitlistId), HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}")
    @Operation(
            summary = "Get Event's Waitlist",
            description = "Retrieve all waitlist entries for a specific event, ordered by position"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Waitlist entries retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<WaitlistResponse>> getWaitlistByEvent(@PathVariable Long eventId) {
        return new ResponseEntity<>(waitlistService.getWaitlistByEvent(eventId), HttpStatus.OK);
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('ATTENDEE')")
    @Operation(
            summary = "Get User's Waitlist Entries",
            description = "Retrieve all waitlist entries for the authenticated attendee"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Waitlist entries retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<WaitlistResponse>> getWaitlistByUser(Authentication authentication) {
        String userEmail = authentication.getName();
        return new ResponseEntity<>(waitlistService.getWaitlistByUser(userEmail), HttpStatus.OK);
    }

    @GetMapping("/position/{eventId}")
    @PreAuthorize("hasRole('ATTENDEE')")
    @Operation(
            summary = "Get Waitlist Position",
            description = "Get the user's position in the waitlist for a specific event (-1 if not on waitlist)"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Position retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Integer> getWaitlistPosition(
            @PathVariable Long eventId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        int position = waitlistService.getWaitlistPosition(eventId, userEmail);
        return new ResponseEntity<>(position, HttpStatus.OK);
    }
}