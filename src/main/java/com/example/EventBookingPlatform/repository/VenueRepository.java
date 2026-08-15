package com.example.EventBookingPlatform.repository;

import com.example.EventBookingPlatform.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, Long> {
}
