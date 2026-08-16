package com.example.EventBookingPlatform.repository;

import com.example.EventBookingPlatform.entity.Event;
import com.example.EventBookingPlatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByIdAndUser(Long eventId, User user);

    List<Event> findByUser(User user);
}