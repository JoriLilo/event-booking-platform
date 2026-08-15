package com.example.EventBookingPlatform.repository;

import com.example.EventBookingPlatform.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
}
