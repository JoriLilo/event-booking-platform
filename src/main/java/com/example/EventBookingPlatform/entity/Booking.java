package com.example.EventBookingPlatform.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private int seatsBooked;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    @Column(nullable = false)
    private LocalDateTime bookingDate;

    @ManyToOne
    @JoinColumn(name = "user_id",  nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "event_id",  nullable = false)
    private Event event;


}
