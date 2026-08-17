package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.BookingRequest;
import com.example.EventBookingPlatform.dto.BookingResponse;
import com.example.EventBookingPlatform.service.BookingService;
import com.example.EventBookingPlatform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("{userId}")
    public ResponseEntity<BookingResponse> createBooking(@Valid  @RequestBody BookingRequest bookingRequest, @PathVariable Long userId) {
        BookingResponse bookingResponse = bookingService.createBooking(bookingRequest, userId);
        return new ResponseEntity<>(bookingResponse, HttpStatus.CREATED);
    }

    @PatchMapping("/{userId}/{bookingId}")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long userId, @PathVariable Long bookingId) {
        return new ResponseEntity<>(bookingService.cancelBooking(bookingId, userId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return new ResponseEntity<>(bookingService.geAllBookings(), HttpStatus.OK);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long bookingId) {
        return new ResponseEntity<>(bookingService.getBookingById(bookingId), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponse>> getBookingByUser(@PathVariable Long userId) {
        return new ResponseEntity<>(bookingService.getBookingsByUser(userId), HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<BookingResponse>> getBookingByEvent(@PathVariable Long eventId) {
        return new ResponseEntity<>(bookingService.getBookingsByEvent(eventId), HttpStatus.OK);
    }

    @PatchMapping
    public ResponseEntity<BookingResponse> adminUpdateBooking(@RequestParam Long bookingId) {
        return new ResponseEntity<>(bookingService.adminCancelBooking(bookingId), HttpStatus.OK);
    }







}
