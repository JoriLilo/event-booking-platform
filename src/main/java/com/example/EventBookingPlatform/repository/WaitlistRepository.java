package com.example.EventBookingPlatform.repository;

import com.example.EventBookingPlatform.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    Optional<Waitlist> findByUserAndEvent(User user, Event event);

    List<Waitlist> findByEventOrderByPosition(Event event);

    @Query("""
        SELECT w FROM Waitlist w
        WHERE w.event.id = :eventId
        AND w.status = 'WAITING'
        ORDER BY w.position ASC
    """)
    List<Waitlist> findWaitingByEventOrderByPosition(@Param("eventId") Long eventId);

    int countByEventAndStatus(Event event, WaitlistStatus status);

    List<Waitlist> findByUserAndStatus(User user, WaitlistStatus status);

    void deleteByUserAndEvent(User user, Event event);
}