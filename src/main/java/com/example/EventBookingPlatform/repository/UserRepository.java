package com.example.EventBookingPlatform.repository;

import com.example.EventBookingPlatform.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
