package com.example.EventBookingPlatform.dto;

import jakarta.validation.constraints.*;
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
public class EventRequest {

    @NotBlank
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull
    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

    @NotNull
    @DecimalMin(value = "0.0")
    private float price;

    @NotNull
    @Min(value = 1)
    private int totalSeats;

    @NotNull
    private Long venueId;

    private List<Long> categoryIds;
}