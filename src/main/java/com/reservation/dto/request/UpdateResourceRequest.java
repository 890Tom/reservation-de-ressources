package com.reservation.dto.request;

import com.reservation.enums.ResourceStatus;
import com.reservation.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateResourceRequest {
    
    private String name;
    private String description;
    private ResourceType type;
    private ResourceStatus status;
    private String location;
    private Integer capacity;
}