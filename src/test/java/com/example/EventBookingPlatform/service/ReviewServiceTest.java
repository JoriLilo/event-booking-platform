package com.example.EventBookingPlatform.service;

import com.example.EventBookingPlatform.dto.ReviewRequest;
import com.example.EventBookingPlatform.dto.ReviewResponse;
import com.example.EventBookingPlatform.entity.*;
import com.example.EventBookingPlatform.exception.EventNotFoundException;
import com.example.EventBookingPlatform.exception.ReviewAlreadyExistsException;
import com.example.EventBookingPlatform.exception.ReviewNotFoundException;
import com.example.EventBookingPlatform.exception.UserNotFoundException;
import com.example.EventBookingPlatform.repository.BookingRepository;
import com.example.EventBookingPlatform.repository.EventRepository;
import com.example.EventBookingPlatform.repository.ReviewRepository;
import com.example.EventBookingPlatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ReviewService reviewService;

    private ReviewRequest reviewRequest;
    private User user;
    private Event event;
    private Review review;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setRole(Role.ATTENDEE);

        Venue venue = new Venue();
        venue.setId(1L);
        venue.setName("Convention Center");

        event = new Event();
        event.setId(1L);
        event.setTitle("Tech Conference");
        event.setStartDateTime(LocalDateTime.now().minusDays(5));
        event.setEndDateTime(LocalDateTime.now().minusDays(4));
        event.setVenue(venue);
        event.setReviews(new ArrayList<>());

        reviewRequest = new ReviewRequest();
        reviewRequest.setEventId(1L);
        reviewRequest.setRating(5);
        reviewRequest.setComment("Great event! Very informative.");

        review = new Review();
        review.setId(1L);
        review.setRating(5);
        review.setComment("Great event! Very informative.");
        review.setEvent(event);
        review.setUser(user);
        review.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateReviewSuccess() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(reviewRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());
        when(bookingRepository.existsByUserAndEventAndStatus(user, event, BookingStatus.CONFIRMED)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        ReviewResponse response = reviewService.createReview(reviewRequest, "john@example.com", 1L);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("Great event! Very informative.", response.getComment());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testCreateReviewDuplicate() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(reviewRepository.findByUserAndEvent(user, event)).thenReturn(Optional.of(review));

        assertThrows(ReviewAlreadyExistsException.class, () ->
                reviewService.createReview(reviewRequest, "john@example.com", 1L));
    }

    @Test
    void testCreateReviewEmptyComment() {
        reviewRequest.setComment("");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(reviewRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                reviewService.createReview(reviewRequest, "john@example.com", 1L));
    }

    @Test
    void testCreateReviewCommentTooShort() {
        reviewRequest.setComment("Bad");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(reviewRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                reviewService.createReview(reviewRequest, "john@example.com", 1L));
    }

    @Test
    void testCreateReviewCommentTooLong() {
        reviewRequest.setComment("x".repeat(251));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(reviewRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                reviewService.createReview(reviewRequest, "john@example.com", 1L));
    }

    @Test
    void testCreateReviewInvalidRating() {
        reviewRequest.setRating(10);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(reviewRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                reviewService.createReview(reviewRequest, "john@example.com", 1L));
    }

    @Test
    void testCreateReviewEventNotFinished() {
        event.setEndDateTime(LocalDateTime.now().plusDays(5));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(reviewRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());
        when(bookingRepository.existsByUserAndEventAndStatus(user, event, BookingStatus.CONFIRMED)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                reviewService.createReview(reviewRequest, "john@example.com", 1L));
    }

    @Test
    void testCreateReviewNoConfirmedBooking() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(reviewRepository.findByUserAndEvent(user, event)).thenReturn(Optional.empty());
        when(bookingRepository.existsByUserAndEventAndStatus(user, event, BookingStatus.CONFIRMED)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () ->
                reviewService.createReview(reviewRequest, "john@example.com", 1L));
    }

    @Test
    void testGetReviewById() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        ReviewResponse response = reviewService.getReviewById(1L);

        assertNotNull(response);
        assertEquals(5, response.getRating());
        assertEquals("Tech Conference", response.getEventTitle());
    }

    @Test
    void testGetReviewByIdNotFound() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ReviewNotFoundException.class, () ->
                reviewService.getReviewById(1L));
    }

    @Test
    void testGetAverageRatingForEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(reviewRepository.getAverageRatingForEvent(1L)).thenReturn(4.5);

        Double averageRating = reviewService.getAverageRatingForEvent(1L);

        assertNotNull(averageRating);
        assertEquals(4.5, averageRating);
    }

    @Test
    void testGetAverageRatingZeroWhenNoReviews() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(reviewRepository.getAverageRatingForEvent(1L)).thenReturn(null);

        Double averageRating = reviewService.getAverageRatingForEvent(1L);

        assertEquals(0.0, averageRating);
    }

    @Test
    void testDeleteReviewById() {
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        reviewService.deleteReviewById(1L, "john@example.com");

        verify(reviewRepository, times(1)).delete(review);
    }
}