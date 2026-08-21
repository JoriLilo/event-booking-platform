package com.example.EventBookingPlatform.service;

import com.example.EventBookingPlatform.dto.VenueRequest;
import com.example.EventBookingPlatform.dto.VenueResponse;
import com.example.EventBookingPlatform.entity.Venue;
import com.example.EventBookingPlatform.repository.VenueRepository;
import com.example.EventBookingPlatform.exception.VenueNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class VenueService {

    private final VenueRepository venueRepository;

    public VenueService(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Transactional
    public VenueResponse addVenue(VenueRequest venueRequest) {

        if (venueRequest.getName() == null || venueRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Venue name cannot be empty");
        }
        if (venueRequest.getAddress() == null || venueRequest.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Venue address cannot be empty");
        }
        if (venueRequest.getCapacity() <= 0) {
            throw new IllegalArgumentException("Venue capacity must be greater than zero");
        }

        Venue venue = new Venue();
        venue.setName(venueRequest.getName());
        venue.setAddress(venueRequest.getAddress());
        venue.setCity(venueRequest.getCity());
        venue.setCapacity(venueRequest.getCapacity());
        venueRepository.save(venue);

        return venueToResponse(venue);
    }

    @Transactional
    public VenueResponse updateVenue(Long venueId, VenueRequest venueRequest) {
        if (venueRequest.getName() == null || venueRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Venue name cannot be empty");
        }
        if (venueRequest.getAddress() == null || venueRequest.getAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Venue address cannot be empty");
        }
        if (venueRequest.getCapacity() <= 0) {
            throw new IllegalArgumentException("Venue capacity must be greater than zero");
        }

        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueNotFoundException("Venue not found"));

        venue.setName(venueRequest.getName());
        venue.setAddress(venueRequest.getAddress());
        venue.setCity(venueRequest.getCity());
        venue.setCapacity(venueRequest.getCapacity());
        venueRepository.save(venue);

        return venueToResponse(venue);
    }

    @Transactional
    public VenueResponse updateVenueCapacity(Long venueId, int capacity) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueNotFoundException("Venue not found"));
        venue.setCapacity(capacity);
        venueRepository.save(venue);
        return venueToResponse(venue);
    }

    public VenueResponse getVenueById(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueNotFoundException("Venue not found"));
        return venueToResponse(venue);
    }

    public List<VenueResponse> getAllVenues() {
        List<Venue> venues = venueRepository.findAll();
        List<VenueResponse> venueResponses = new ArrayList<>();
        for (Venue venue: venues) {
            venueResponses.add(venueToResponse(venue));
        }
        return venueResponses;
    }

    @Transactional
    public void deleteVenue(Long venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new VenueNotFoundException("Venue not found"));
        venueRepository.delete(venue);
    }

    public VenueResponse venueToResponse(Venue venue) {
        VenueResponse response = new VenueResponse();
        response.setId(venue.getId());
        response.setName(venue.getName());
        response.setAddress(venue.getAddress());
        response.setCity(venue.getCity());
        response.setCapacity(venue.getCapacity());
        return response;
    }
}