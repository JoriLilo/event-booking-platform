package com.example.EventBookingPlatform.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column
    private String description;
    @Column(nullable = false)
    private LocalDateTime startDateTime;
    @Column
    private LocalDateTime endDateTime;
    @Column(nullable = false)
    private float price;
    @Column(nullable = false)
    private int totalSeats;
    @Column(nullable = false)
    private int availableSeats;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer;

    @ManyToOne
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @OneToMany(mappedBy = "event")
    private List<Review> reviews;

    @ManyToMany(mappedBy = "event")
    @JoinTable(name = "event_category")
    private List<Category> categories;

    @OneToMany(mappedBy = "event")
    private List<Booking> bookings;
}
