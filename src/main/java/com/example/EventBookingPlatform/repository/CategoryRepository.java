package com.example.EventBookingPlatform.repository;

import com.example.EventBookingPlatform.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
