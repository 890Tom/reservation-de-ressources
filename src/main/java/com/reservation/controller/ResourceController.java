package com.reservation.controller;

import com.reservation.dto.request.CreateResourceRequest;
import com.reservation.dto.request.UpdateResourceRequest;
import com.reservation.dto.response.AvailabilityResponse;
import com.reservation.dto.response.MessageResponse;
import com.reservation.dto.response.ResourceResponse;
import com.reservation.enums.ResourceStatus;
import com.reservation.enums.ResourceType;
import com.reservation.exception.ResourceNotFoundException;
import com.reservation.service.ResourceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/resources")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ResourceController {
    
    private final ResourceService resourceService;
    
    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }
    
    /**
     * POST /api/resources
     * Créer une nouvelle ressource
     * Accessible par : MANAGER, ADMIN
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> createResource(@Valid @RequestBody CreateResourceRequest request) {
        
        try {
            ResourceResponse resourceResponse = resourceService.createResource(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(resourceResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la création de la ressource"));
        }
    }
    
    /**
     * GET /api/resources
     * Lister toutes les ressources avec filtres et pagination
     * Accessible par : tous les utilisateurs authentifiés
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ResourceResponse>> getAllResources(
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) ResourceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        
        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            Page<ResourceResponse> resources = resourceService.getAllResources(type, status, pageable);
            
            return ResponseEntity.ok(resources);
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * GET /api/resources/{id}
     * Récupérer une ressource par son ID
     * Accessible par : tous les utilisateurs authentifiés
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> getResourceById(@PathVariable Long id) {
        
        try {
            ResourceResponse resourceResponse = resourceService.getResourceById(id);
            return ResponseEntity.ok(resourceResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la récupération de la ressource"));
        }
    }
    
    /**
     * PUT /api/resources/{id}
     * Mettre à jour une ressource
     * Accessible par : MANAGER, ADMIN
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody UpdateResourceRequest request
    ) {
        
        try {
            ResourceResponse resourceResponse = resourceService.updateResource(id, request);
            return ResponseEntity.ok(resourceResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la mise à jour de la ressource"));
        }
    }
    
    /**
     * DELETE /api/resources/{id}
     * Supprimer une ressource
     * Accessible par : ADMIN uniquement
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteResource(@PathVariable Long id) {
        
        try {
            resourceService.deleteResource(id);
            return ResponseEntity.ok(new MessageResponse("Ressource supprimée avec succès"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la suppression de la ressource"));
        }
    }
    
    /**
     * GET /api/resources/{id}/availability
     * Vérifier la disponibilité d'une ressource sur un créneau
     * Accessible par : tous les utilisateurs authentifiés
     */
    @GetMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> checkAvailability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        
        try {
            AvailabilityResponse availabilityResponse = resourceService.checkAvailability(id, startTime, endTime);
            return ResponseEntity.ok(availabilityResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la vérification de disponibilité"));
        }
    }
}