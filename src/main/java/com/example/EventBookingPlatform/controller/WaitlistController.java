package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.WaitlistRequest;
import com.example.EventBookingPlatform.dto.WaitlistResponse;
import com.example.EventBookingPlatform.service.WaitlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/waitlist")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @PostMapping
    public ResponseEntity<WaitlistResponse> addToWaitlist(
            @Valid @RequestBody WaitlistRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        return new ResponseEntity<>(
                waitlistService.addToWaitlist(request, userEmail),
                HttpStatus.CREATED
        );
    }

    @DeleteMapping("/{waitlistId}")
    public ResponseEntity<Void> removeFromWaitlist(
            @PathVariable Long waitlistId,
            Authentication authentication) {

        String userEmail = authentication.getName();
        waitlistService.removeFromWaitlist(waitlistId, userEmail);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{waitlistId}")
    public ResponseEntity<WaitlistResponse> getWaitlistById(@PathVariable Long waitlistId) {

        return new ResponseEntity<>(
                waitlistService.getWaitlistById(waitlistId),
                HttpStatus.OK
        );
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<WaitlistResponse>> getWaitlistByEvent(@PathVariable Long eventId) {

        return new ResponseEntity<>(
                waitlistService.getWaitlistByEvent(eventId),
                HttpStatus.OK
        );
    }

    @GetMapping("/user")
    public ResponseEntity<List<WaitlistResponse>> getWaitlistByUser(Authentication authentication) {

        String userEmail = authentication.getName();
        return new ResponseEntity<>(
                waitlistService.getWaitlistByUser(userEmail),
                HttpStatus.OK
        );
    }

    @GetMapping("/position/{eventId}")
    public ResponseEntity<Integer> getWaitlistPosition(
            @PathVariable Long eventId,
            Authentication authentication) {

        String userEmail = authentication.getName();
        int position = waitlistService.getWaitlistPosition(eventId, userEmail);

        return new ResponseEntity<>(position, HttpStatus.OK);
    }
}