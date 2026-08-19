package com.example.EventBookingPlatform.repository;

import com.example.EventBookingPlatform.entity.Booking;
import com.example.EventBookingPlatform.entity.BookingStatus;
import com.example.EventBookingPlatform.entity.Event;
import com.example.EventBookingPlatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByUserAndEventAndStatus(User user, Event event, BookingStatus bookingStatus);
}
