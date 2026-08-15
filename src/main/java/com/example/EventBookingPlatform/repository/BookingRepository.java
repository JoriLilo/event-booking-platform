package com.example.EventBookingPlatform.repository;

import com.example.EventBookingPlatform.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
}
