package com.example.EventBookingPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    private Long id;
    private String eventTitle;
    private String attendeeUsername;
    private int seatsBooked;
    private String status;
    private LocalDateTime bookingDate;
}