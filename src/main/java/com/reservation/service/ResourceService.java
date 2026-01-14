package com.reservation.service;

import com.reservation.dto.request.CreateResourceRequest;
import com.reservation.dto.request.UpdateResourceRequest;
import com.reservation.dto.response.AvailabilityResponse;
import com.reservation.dto.response.ResourceResponse;
import com.reservation.entity.Resource;
import com.reservation.enums.ResourceStatus;
import com.reservation.enums.ResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface ResourceService {
    
    
    // Créer une nouvelle ressource (ADMIN/MANAGER)
    ResourceResponse createResource(CreateResourceRequest request);
    
    // Lister toutes les ressources avec filtres et pagination
    Page<ResourceResponse> getAllResources(ResourceType type, ResourceStatus status, Pageable pageable);
    
    // Récupérer une ressource par son ID
    ResourceResponse getResourceById(Long id);
    
    // Mettre à jour une ressource (ADMIN/MANAGER)
    ResourceResponse updateResource(Long id, UpdateResourceRequest request);
    
    // Supprimer une ressource (ADMIN uniquement)
    void deleteResource(Long id);
    
    // Vérifier la disponibilité d'une ressource sur un créneau
    AvailabilityResponse checkAvailability(Long resourceId, LocalDateTime startTime, LocalDateTime endTime);
    
    // Convertir Resource en ResourceResponse
    ResourceResponse toResourceResponse(Resource resource);
}