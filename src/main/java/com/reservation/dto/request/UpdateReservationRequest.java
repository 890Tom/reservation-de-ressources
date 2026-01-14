package com.reservation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReservationRequest {
    
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String purpose;
}