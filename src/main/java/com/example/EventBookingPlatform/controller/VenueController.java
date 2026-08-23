package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.VenueRequest;
import com.example.EventBookingPlatform.dto.VenueResponse;
import com.example.EventBookingPlatform.service.VenueService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venue")
@Tag(name = "Venues", description = "Venue management endpoints")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Create Venue",
            description = "Admin creates a new venue. Requires ADMIN role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Venue created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation error"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions (not an admin)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<VenueResponse> createVenue(@Valid @RequestBody VenueRequest venueRequest) {
        return new ResponseEntity<>(venueService.addVenue(venueRequest), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Get All Venues",
            description = "Retrieve all venues (public, no authentication required)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venues retrieved successfully")
    })
    public ResponseEntity<List<VenueResponse>> getAllVenues() {
        return new ResponseEntity<>(venueService.getAllVenues(), HttpStatus.OK);
    }

    @GetMapping("/{venueId}")
    @Operation(
            summary = "Get Venue Details",
            description = "Retrieve details of a specific venue"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venue retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Venue not found")
    })
    public ResponseEntity<VenueResponse> getVenueById(@PathVariable Long venueId) {
        return new ResponseEntity<>(venueService.getVenueById(venueId), HttpStatus.OK);
    }

    @PutMapping("/{venueId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update Venue",
            description = "Admin updates venue details. Requires ADMIN role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venue updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or validation error"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions (not an admin)"),
            @ApiResponse(responseCode = "404", description = "Venue not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<VenueResponse> updateVenue(
            @PathVariable Long venueId,
            @Valid @RequestBody VenueRequest venueRequest) {
        return new ResponseEntity<>(venueService.updateVenue(venueId, venueRequest), HttpStatus.OK);
    }

    @DeleteMapping("/{venueId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete Venue",
            description = "Admin deletes a venue. Requires ADMIN role."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venue deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions (not an admin)"),
            @ApiResponse(responseCode = "404", description = "Venue not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteVenue(@PathVariable Long venueId) {
        venueService.deleteVenue(venueId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}