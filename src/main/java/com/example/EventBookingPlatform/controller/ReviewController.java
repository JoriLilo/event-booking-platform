package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.ReviewRequest;
import com.example.EventBookingPlatform.dto.ReviewResponse;
import com.example.EventBookingPlatform.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/event/{eventId}")
    public ResponseEntity<ReviewResponse> createReview(@PathVariable Long eventId, @RequestBody ReviewRequest reviewRequest, Authentication authentication) {
        String userEmail = authentication.getName();
        return new ResponseEntity<>(reviewService.createReview(reviewRequest, userEmail, eventId), HttpStatus.CREATED);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long reviewId) {

        return new ResponseEntity<>(reviewService.getReviewById(reviewId), HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByEvent(@PathVariable Long eventId) {

        return new ResponseEntity<>(reviewService.getReviewsByEvent(eventId), HttpStatus.OK);
    }

    @GetMapping("/user")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUser(Authentication authentication) {

        String userEmail = authentication.getName();

        return new ResponseEntity<>(reviewService.getReviewsByUser(userEmail), HttpStatus.OK);
    }
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReviewById(@PathVariable Long reviewId, Authentication authentication) {

        String userEmail = authentication.getName();
        reviewService.deleteReviewById(reviewId, userEmail);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/event/{eventId}/average-rating")
    public ResponseEntity<Double> getAverageRatingForEvent(@PathVariable Long eventId) {

        return new ResponseEntity<>(reviewService.getAverageRatingForEvent(eventId), HttpStatus.OK);
    }
}