package com.reservation.dto.request;

import com.reservation.enums.ResourceStatus;
import com.reservation.enums.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateResourceRequest {
    
    @NotBlank(message = "Le nom de la ressource est obligatoire")
    private String name;
    
    private String description;
    
    @NotNull(message = "Le type de ressource est obligatoire")
    private ResourceType type;
    
    @NotNull(message = "Le statut est obligatoire")
    private ResourceStatus status;
    
    private String location;
    
    private Integer capacity;
}