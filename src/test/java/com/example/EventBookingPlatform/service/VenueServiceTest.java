package com.example.EventBookingPlatform.service;

import com.example.EventBookingPlatform.dto.VenueRequest;
import com.example.EventBookingPlatform.dto.VenueResponse;
import com.example.EventBookingPlatform.entity.Venue;
import com.example.EventBookingPlatform.exception.VenueNotFoundException;
import com.example.EventBookingPlatform.repository.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueService venueService;

    private VenueRequest venueRequest;
    private Venue venue;

    @BeforeEach
    void setUp() {
        venueRequest = new VenueRequest();
        venueRequest.setName("Convention Center");
        venueRequest.setAddress("123 Main Street");
        venueRequest.setCity("New York");
        venueRequest.setCapacity(500);

        venue = new Venue();
        venue.setId(1L);
        venue.setName("Convention Center");
        venue.setAddress("123 Main Street");
        venue.setCity("New York");
        venue.setCapacity(500);
    }

    @Test
    void testAddVenueSuccess() {
        when(venueRepository.save(any(Venue.class))).thenReturn(venue);

        VenueResponse response = venueService.addVenue(venueRequest);

        assertNotNull(response);
        assertEquals("Convention Center", response.getName());
        assertEquals("New York", response.getCity());
        assertEquals(500, response.getCapacity());
        verify(venueRepository, times(1)).save(any(Venue.class));
    }

    @Test
    void testAddVenueWithEmptyName() {
        venueRequest.setName("");

        assertThrows(IllegalArgumentException.class, () ->
                venueService.addVenue(venueRequest));
    }

    @Test
    void testAddVenueWithEmptyAddress() {
        venueRequest.setAddress("");

        assertThrows(IllegalArgumentException.class, () ->
                venueService.addVenue(venueRequest));
    }

    @Test
    void testAddVenueWithZeroCapacity() {
        venueRequest.setCapacity(0);

        assertThrows(IllegalArgumentException.class, () ->
                venueService.addVenue(venueRequest));
    }

    @Test
    void testGetVenueById() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));

        VenueResponse response = venueService.getVenueById(1L);

        assertNotNull(response);
        assertEquals("Convention Center", response.getName());
    }

    @Test
    void testGetVenueByIdNotFound() {
        when(venueRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(VenueNotFoundException.class, () ->
                venueService.getVenueById(1L));
    }

    @Test
    void testGetAllVenues() {
        List<Venue> venues = new ArrayList<>();
        venues.add(venue);
        when(venueRepository.findAll()).thenReturn(venues);

        List<VenueResponse> responses = venueService.getAllVenues();

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(venueRepository, times(1)).findAll();
    }

    @Test
    void testUpdateVenueSuccess() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(venueRepository.save(any(Venue.class))).thenReturn(venue);

        VenueRequest updateRequest = new VenueRequest();
        updateRequest.setName("Updated Center");
        updateRequest.setAddress("456 Oak Avenue");
        updateRequest.setCity("Los Angeles");
        updateRequest.setCapacity(600);

        VenueResponse response = venueService.updateVenue(1L, updateRequest);

        assertNotNull(response);
        verify(venueRepository, times(1)).save(any(Venue.class));
    }

    @Test
    void testUpdateVenueNotFound() {
        when(venueRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(VenueNotFoundException.class, () ->
                venueService.updateVenue(1L, venueRequest));
    }

    @Test
    void testUpdateVenueCapacity() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));
        when(venueRepository.save(any(Venue.class))).thenReturn(venue);

        VenueResponse response = venueService.updateVenueCapacity(1L, 750);

        assertNotNull(response);
        verify(venueRepository, times(1)).save(any(Venue.class));
    }

    @Test
    void testUpdateVenueCapacityNotFound() {
        when(venueRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(VenueNotFoundException.class, () ->
                venueService.updateVenueCapacity(1L, 750));
    }

    @Test
    void testDeleteVenue() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(venue));

        venueService.deleteVenue(1L);

        verify(venueRepository, times(1)).delete(venue);
    }

    @Test
    void testDeleteVenueNotFound() {
        when(venueRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(VenueNotFoundException.class, () ->
                venueService.deleteVenue(1L));
    }
}