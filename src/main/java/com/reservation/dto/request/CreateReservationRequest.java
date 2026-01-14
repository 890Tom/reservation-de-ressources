package com.reservation.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReservationRequest {
    
    private Long resourceId;
    
    @NotNull(message = "La date de début est obligatoire")
    private LocalDateTime startTime;
    
    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime endTime;
    
    private String purpose;
}