package com.example.EventBookingPlatform.controller;

import com.example.EventBookingPlatform.dto.ReviewRequest;
import com.example.EventBookingPlatform.dto.ReviewResponse;
import com.example.EventBookingPlatform.service.ReviewService;
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
@RequestMapping("/api/review")
@Tag(name = "Reviews", description = "Event review management and ratings")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ATTENDEE')")
    @Operation(
            summary = "Create Review",
            description = "Attendee creates a review for a past event. Must have a confirmed booking and event must be completed."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid review (not eligible, event not completed, etc.)"),
            @ApiResponse(responseCode = "403", description = "Insufficient permissions (not an attendee)"),
            @ApiResponse(responseCode = "404", description = "Event not found"),
            @ApiResponse(responseCode = "409", description = "Review already exists for this event"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ReviewResponse> createReview(
            @PathVariable Long eventId,
            @Valid @RequestBody ReviewRequest reviewRequest,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return new ResponseEntity<>(reviewService.createReview(reviewRequest, userEmail, eventId), HttpStatus.CREATED);
    }

    @GetMapping("/{reviewId}")
    @Operation(
            summary = "Get Review Details",
            description = "Retrieve details of a specific review"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ReviewResponse> getReviewById(@PathVariable Long reviewId) {
        return new ResponseEntity<>(reviewService.getReviewById(reviewId), HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}")
    @Operation(
            summary = "Get Event Reviews",
            description = "Retrieve all reviews for a specific event"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<List<ReviewResponse>> getReviewsByEvent(@PathVariable Long eventId) {
        return new ResponseEntity<>(reviewService.getReviewsByEvent(eventId), HttpStatus.OK);
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('ATTENDEE')")
    @Operation(
            summary = "Get User's Reviews",
            description = "Retrieve all reviews written by the authenticated attendee"
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<ReviewResponse>> getReviewsByUser(Authentication authentication) {
        String userEmail = authentication.getName();
        return new ResponseEntity<>(reviewService.getReviewsByUser(userEmail), HttpStatus.OK);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ATTENDEE')")
    @Operation(
            summary = "Delete Review",
            description = "Attendee deletes their own review. Only the review creator can delete it."
    )
    @SecurityRequirement(name = "Bearer Authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Not your review"),
            @ApiResponse(responseCode = "404", description = "Review not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {
        String userEmail = authentication.getName();
        reviewService.deleteReviewById(reviewId, userEmail);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}