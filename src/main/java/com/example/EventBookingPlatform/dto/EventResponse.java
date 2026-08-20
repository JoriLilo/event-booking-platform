package com.example.EventBookingPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {

    private Long id;
    private String title;
    private String description;
    private Double averageRating;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private float price;
    private int totalSeats;
    private String status;
    private String organizerUsername;
    private String venueName;
    private List<String> categoryNames;

}