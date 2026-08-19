package com.example.EventBookingPlatform.repository;

import com.example.EventBookingPlatform.entity.Event;
import com.example.EventBookingPlatform.entity.Review;
import com.example.EventBookingPlatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Optional<Review> findByUserAndEvent(User thisUserDoesNotExist, Event byId);

    List<Review> findByEvent(Event eventId);

    List<Review> findByUser(User user);
}
