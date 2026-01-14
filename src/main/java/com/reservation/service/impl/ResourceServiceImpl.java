package com.reservation.service.impl;

import com.reservation.dto.request.CreateResourceRequest;
import com.reservation.dto.request.UpdateResourceRequest;
import com.reservation.dto.response.AvailabilityResponse;
import com.reservation.dto.response.ResourceResponse;
import com.reservation.entity.Reservation;
import com.reservation.entity.Resource;
import com.reservation.enums.ResourceStatus;
import com.reservation.enums.ResourceType;
import com.reservation.exception.ResourceNotFoundException;
import com.reservation.repository.ReservationRepository;
import com.reservation.repository.ResourceRepository;
import com.reservation.service.ResourceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResourceServiceImpl implements ResourceService {
    
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    
    public ResourceServiceImpl(ResourceRepository resourceRepository,
                              ReservationRepository reservationRepository) {
        this.resourceRepository = resourceRepository;
        this.reservationRepository = reservationRepository;
    }
    
    @Override
    public ResourceResponse createResource(CreateResourceRequest request) {
        
        Resource resource = new Resource();
        resource.setName(request.getName());
        resource.setDescription(request.getDescription());
        resource.setType(request.getType());
        resource.setStatus(request.getStatus());
        resource.setLocation(request.getLocation());
        resource.setCapacity(request.getCapacity());
        
        Resource savedResource = resourceRepository.save(resource);
        
        return toResourceResponse(savedResource);
    }
    
    @Override
    public Page<ResourceResponse> getAllResources(ResourceType type, ResourceStatus status, Pageable pageable) {
        
        Page<Resource> resources;
        
        if (type != null && status != null) {
            resources = resourceRepository.findByTypeAndStatus(type, status, pageable);
        } else if (type != null) {
            resources = resourceRepository.findByType(type, pageable);
        } else if (status != null) {
            resources = resourceRepository.findByStatus(status, pageable);
        } else {
            resources = resourceRepository.findAll(pageable);
        }
        
        return resources.map(this::toResourceResponse);
    }
    
    @Override
    public ResourceResponse getResourceById(Long id) {
        
        Resource resource = resourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ressource non trouvée avec l'ID: " + id));
        
        return toResourceResponse(resource);
    }
    
    @Override
    public ResourceResponse updateResource(Long id, UpdateResourceRequest request) {
        
        Resource resource = resourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ressource non trouvée avec l'ID: " + id));
        
        // Mettre à jour uniquement les champs fournis
        if (StringUtils.hasText(request.getName())) {
            resource.setName(request.getName());
        }
        if (request.getDescription() != null) {
            resource.setDescription(request.getDescription());
        }
        if (request.getType() != null) {
            resource.setType(request.getType());
        }
        if (request.getStatus() != null) {
            resource.setStatus(request.getStatus());
        }
        if (request.getLocation() != null) {
            resource.setLocation(request.getLocation());
        }
        if (request.getCapacity() != null) {
            resource.setCapacity(request.getCapacity());
        }
        
        Resource updatedResource = resourceRepository.save(resource);
        
        return toResourceResponse(updatedResource);
    }
    
    @Override
    public void deleteResource(Long id) {
        
        Resource resource = resourceRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Ressource non trouvée avec l'ID: " + id));
        
        // Vérifier s'il y a des réservations actives
        Long activeReservations = reservationRepository.countActiveReservationsByResourceId(id);
        if (activeReservations > 0) {
            throw new IllegalStateException(
                "Impossible de supprimer la ressource. Elle a " + activeReservations + " réservation(s) active(s)."
            );
        }
        
        resourceRepository.delete(resource);
    }
    
    @Override
    public AvailabilityResponse checkAvailability(Long resourceId, LocalDateTime startTime, LocalDateTime endTime) {
        
        // Vérifier que la ressource existe
        Resource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new ResourceNotFoundException("Ressource non trouvée avec l'ID: " + resourceId));
        
        // Rechercher les réservations en conflit
        List<Reservation> conflictingReservations = reservationRepository.findConflictingReservations(
            resourceId, startTime, endTime
        );
        
        boolean available = conflictingReservations.isEmpty();
        
        // Créer la liste des conflits
        List<AvailabilityResponse.ConflictInfo> conflicts = conflictingReservations.stream()
            .map(reservation -> new AvailabilityResponse.ConflictInfo(
                reservation.getId(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getUser().getUsername()
            ))
            .collect(Collectors.toList());
        
        return new AvailabilityResponse(
            resourceId,
            resource.getName(),
            available,
            startTime,
            endTime,
            conflicts
        );
    }
    
    @Override
    public ResourceResponse toResourceResponse(Resource resource) {
        ResourceResponse response = new ResourceResponse();
        response.setId(resource.getId());
        response.setName(resource.getName());
        response.setDescription(resource.getDescription());
        response.setType(resource.getType());
        response.setStatus(resource.getStatus());
        response.setLocation(resource.getLocation());
        response.setCapacity(resource.getCapacity());
        response.setCreatedAt(resource.getCreatedAt());
        response.setUpdatedAt(resource.getUpdatedAt());
        return response;
    }
}