package com.example.EventBookingPlatform.repository;

import com.example.EventBookingPlatform.entity.Event;
import com.example.EventBookingPlatform.entity.Review;
import com.example.EventBookingPlatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByUserAndEvent(User user, Event event);
    List<Review> findByEvent(Event event);

    @Query("""
    SELECT AVG(r.rating)
    FROM Review r
    WHERE r.event.id = :eventId
""")
    Double getAverageRatingForEvent(@Param("eventId") Long eventId);
}
