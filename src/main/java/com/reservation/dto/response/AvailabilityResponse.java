package com.reservation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityResponse {
    
    private Long resourceId;
    private String resourceName;
    private boolean available;
    private LocalDateTime requestedStartTime;
    private LocalDateTime requestedEndTime;
    private List<ConflictInfo> conflicts;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConflictInfo {
        private Long reservationId;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String reservedBy;
    }
}