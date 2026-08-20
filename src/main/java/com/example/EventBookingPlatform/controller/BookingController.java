package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.BookingRequest;
import com.example.EventBookingPlatform.dto.BookingResponse;
import com.example.EventBookingPlatform.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest bookingRequest, Authentication authentication) {
        String userEmail = authentication.getName();
        BookingResponse bookingResponse = bookingService.createBooking(bookingRequest, userEmail);

        return new ResponseEntity<>(bookingResponse, HttpStatus.CREATED);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long bookingId, Authentication authentication) {

        String userEmail = authentication.getName();
        return new ResponseEntity<>(bookingService.cancelBooking(bookingId, userEmail), HttpStatus.OK);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return new ResponseEntity<>(bookingService.geAllBookings(), HttpStatus.OK);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long bookingId) {

        return new ResponseEntity<>(bookingService.getBookingById(bookingId), HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<List<BookingResponse>> getBookingByUser(Authentication authentication) {
        String userEmail = authentication.getName();
        return new ResponseEntity<>(bookingService.getBookingsByUser(userEmail), HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<BookingResponse>> getBookingByEvent(@PathVariable Long eventId) {

        return new ResponseEntity<>(bookingService.getBookingsByEvent(eventId), HttpStatus.OK);
    }

    @PatchMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BookingResponse> adminUpdateBooking(@RequestParam Long bookingId) {

        return new ResponseEntity<>(bookingService.adminCancelBooking(bookingId), HttpStatus.OK);
    }
}