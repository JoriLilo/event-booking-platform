package com.example.EventBookingPlatform.service;

import com.example.EventBookingPlatform.dto.ReviewRequest;
import com.example.EventBookingPlatform.dto.ReviewResponse;
import com.example.EventBookingPlatform.entity.BookingStatus;
import com.example.EventBookingPlatform.entity.Event;
import com.example.EventBookingPlatform.entity.Review;
import com.example.EventBookingPlatform.entity.User;
import com.example.EventBookingPlatform.exception.EventNotFoundException;
import com.example.EventBookingPlatform.exception.ReviewAlreadyExistsException;
import com.example.EventBookingPlatform.exception.ReviewNotFoundException;
import com.example.EventBookingPlatform.exception.UserNotFoundException;
import com.example.EventBookingPlatform.repository.BookingRepository;
import com.example.EventBookingPlatform.repository.EventRepository;
import com.example.EventBookingPlatform.repository.ReviewRepository;
import com.example.EventBookingPlatform.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;

    public ReviewService(ReviewRepository reviewRepository, UserRepository userRepository, EventRepository eventRepository,BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
    }

    public ReviewResponse createReview(ReviewRequest reviewRequest, Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("this user does not exist"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
        Optional<Review> review = reviewRepository.findByUserAndEvent(user, event);
        if (review.isPresent()) {
            throw new ReviewAlreadyExistsException("Review already exists");
        }
        if(reviewRequest.getComment() == null || reviewRequest.getComment().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }
        if(reviewRequest.getComment().length() > 250) {
            throw new IllegalArgumentException("Comment cannot be longer than 250 characters");
        }
        if(reviewRequest.getComment().length() < 5) {
            throw new IllegalArgumentException("Comment cannot be shorter than 5 characters");
        }
        if (reviewRequest.getRating() == null ) {
            throw new IllegalArgumentException("Rating cannot be empty");
        }

        if (reviewRequest.getRating() < 1 || reviewRequest.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        boolean hasConfirmedBooking = bookingRepository.existsByUserAndEventAndStatus(user, event, BookingStatus.CONFIRMED);
        if (!hasConfirmedBooking) {
            throw new IllegalArgumentException("You must have a confirmed booking for this event to review it");
        }
        if (event.getEndDateTime() == null || event.getEndDateTime().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("You can only review an event after it has taken place");
        }

        Review reviewEntity = new Review();
        reviewEntity.setEvent(event);
        reviewEntity.setRating(reviewRequest.getRating());
        reviewEntity.setComment(reviewRequest.getComment());
        reviewEntity.setCreatedAt(LocalDateTime.now());
        reviewEntity.setUser(user);
        reviewRepository.save(reviewEntity);

        return reviewToResponse(reviewEntity);
    }


    public ReviewResponse getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found"));
        return reviewToResponse(review);
    }

    public List<ReviewResponse> getReviewsByEvent(Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new EventNotFoundException("Event not found"));
        List<Review> reviews = reviewRepository.findByEvent(event);
        List<ReviewResponse> responses = new ArrayList<>();
        for (Review review : reviews) {
            responses.add(reviewToResponse(review));

        }
        return responses;
    }

    public List<ReviewResponse> getReviewsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("this user does not exist"));

        List<Review> reviews = reviewRepository.findByUser(user);
        List<ReviewResponse> responses = new ArrayList<>();
        for (Review review : reviews) {
            responses.add(reviewToResponse(review));
        }
        return responses;
    }

    public void deleteReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                        .orElseThrow(() -> new ReviewNotFoundException("Review not found"));
        reviewRepository.deleteById(reviewId);
    }

    public float getAverageRatingForEvent(Long eventId) {
        List<ReviewResponse> responses = getReviewsByEvent(eventId);
        float averageRating = 0;
        for (ReviewResponse reviewResponse : responses) {
            averageRating += reviewResponse.getRating();

        }
        return averageRating / responses.size();
    }

    public ReviewResponse reviewToResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setEventTitle(review.getEvent().getTitle());
        response.setReviewerUsername(review.getUser().getUsername());
        response.setRating(review.getRating());
        response.setComment(review.getComment());
        response.setCreatedAt(review.getCreatedAt());
        return response;
    }
}
