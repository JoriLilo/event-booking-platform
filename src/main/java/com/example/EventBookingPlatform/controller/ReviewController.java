package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.ReviewRequest;
import com.example.EventBookingPlatform.dto.ReviewResponse;
import com.example.EventBookingPlatform.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/user/{userId}/event/{eventId}")
    public ResponseEntity<ReviewResponse> createReview(@PathVariable Long userId, @PathVariable Long eventId, @RequestBody ReviewRequest reviewRequest) {

        return new ResponseEntity<>(reviewService.createReview(reviewRequest, userId, eventId), HttpStatus.CREATED);
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long reviewId) {

        return new ResponseEntity<>(reviewService.getReviewById(reviewId), HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByEvent(@PathVariable Long eventId) {

        return new ResponseEntity<>(reviewService.getReviewsByEvent(eventId), HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUser(@PathVariable Long userId) {

        return new ResponseEntity<>(reviewService.getReviewsByUser(userId), HttpStatus.OK);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReviewById(@PathVariable Long reviewId) {

        reviewService.deleteReviewById(reviewId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}/average-rating")
    public ResponseEntity<Float> getAverageRatingForEvent(@PathVariable Long eventId) {

        return new ResponseEntity<>(reviewService.getAverageRatingForEvent(eventId), HttpStatus.OK);
    }
}