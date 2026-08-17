package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.VenueRequest;
import com.example.EventBookingPlatform.dto.VenueResponse;
import com.example.EventBookingPlatform.service.VenueService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/venue")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    public ResponseEntity<VenueResponse> addVenue(@RequestBody VenueRequest venueRequest) {
        return new ResponseEntity<>(venueService.addVenue(venueRequest), HttpStatus.CREATED);
    }

    @GetMapping("/{venueId}")
    public ResponseEntity<VenueResponse> viewVenue(@PathVariable Long venueId) {
        return new ResponseEntity<>(venueService.getVenueById(venueId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<VenueResponse>> viewAllVenues() {
        return new ResponseEntity<>(venueService.getAllVenues(), HttpStatus.OK);
    }

    @PutMapping("/{venueId}")
    public ResponseEntity<VenueResponse> updateVenue(@PathVariable Long venueId, @RequestBody VenueRequest venueRequest) {
        return new ResponseEntity<>(venueService.updateVenue(venueId, venueRequest), HttpStatus.OK);
    }

    @PatchMapping("/{venueId}/capacity")
    public ResponseEntity<VenueResponse> updateVenueCapacity(@PathVariable Long venueId, @RequestParam int  capacity) {
        return new ResponseEntity<>(venueService.updateVenueCapacity(venueId,capacity), HttpStatus.OK);
    }

    @DeleteMapping("/{venueId}")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long venueId) {
        venueService.deleteVenue(venueId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
