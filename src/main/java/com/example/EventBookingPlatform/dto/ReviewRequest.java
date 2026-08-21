package com.example.EventBookingPlatform.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequest {

    @NotNull
    private Long eventId;

    @NotNull
    @Min(value = 1)
    @Max(value = 5)
    private Integer rating;

    @NotBlank
    @Size(min = 5, max = 250)
    private String comment;
}