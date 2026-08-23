package com.example.EventBookingPlatform.repository;

import com.example.EventBookingPlatform.entity.Event;
import com.example.EventBookingPlatform.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByIdAndUser(Long eventId, User user);

    List<Event> findByUser(User user);

    // Search by title or description (case-insensitive)
    @Query("""
        SELECT e FROM Event e
        WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        ORDER BY e.startDateTime ASC
    """)
    Page<Event> searchByTitleOrDescription(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Filter by date range
    @Query("""
        SELECT e FROM Event e
        WHERE e.startDateTime >= :startDate
        AND e.startDateTime <= :endDate
        ORDER BY e.startDateTime ASC
    """)
    Page<Event> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    // Filter by price range
    @Query("""
        SELECT e FROM Event e
        WHERE e.price >= :minPrice
        AND e.price <= :maxPrice
        ORDER BY e.price ASC
    """)
    Page<Event> findByPriceRange(
            @Param("minPrice") float minPrice,
            @Param("maxPrice") float maxPrice,
            Pageable pageable
    );

    // Filter by category
    @Query("""
        SELECT DISTINCT e FROM Event e
        JOIN e.categories c
        WHERE c.id = :categoryId
        ORDER BY e.startDateTime ASC
    """)
    Page<Event> findByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    // Filter by city (via venue)
    Page<Event> findByVenueCity(String city, Pageable pageable);

    // Combined search with multiple filters
    @Query("""
        SELECT e FROM Event e
        WHERE (LOWER(e.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        OR LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))
        AND e.startDateTime >= :startDate
        AND e.startDateTime <= :endDate
        AND e.price >= :minPrice
        AND e.price <= :maxPrice
        ORDER BY e.startDateTime ASC
    """)
    Page<Event> searchWithFilters(
            @Param("searchTerm") String searchTerm,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minPrice") float minPrice,
            @Param("maxPrice") float maxPrice,
            Pageable pageable
    );

    // Find upcoming events
    @Query("""
        SELECT e FROM Event e
        WHERE e.startDateTime > :now
        ORDER BY e.startDateTime ASC
    """)
    Page<Event> findUpcomingEvents(@Param("now") LocalDateTime now, Pageable pageable);

    // Find by venue
    @Query("""
        SELECT e FROM Event e
        WHERE e.venue.id = :venueId
        ORDER BY e.startDateTime ASC
    """)
    Page<Event> findByVenue(@Param("venueId") Long venueId, Pageable pageable);
}