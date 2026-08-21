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
public class WaitlistResponse {

    private Long id;
    private String eventTitle;
    private String userUsername;
    private int position;
    private String status;
    private LocalDateTime addedAt;
}