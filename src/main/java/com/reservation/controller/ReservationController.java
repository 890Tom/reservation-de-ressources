package com.reservation.controller;

import com.reservation.dto.request.CreateReservationRequest;
import com.reservation.dto.request.UpdateReservationRequest;
import com.reservation.dto.response.MessageResponse;
import com.reservation.dto.response.ReservationResponse;
import com.reservation.exception.InvalidReservationException;
import com.reservation.exception.ReservationConflictException;
import com.reservation.exception.ResourceNotFoundException;
import com.reservation.exception.UnauthorizedException;
import com.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReservationController {
    
    private final ReservationService reservationService;
    
    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }
    
    /**
     * POST /api/reservations
     * Créer une nouvelle réservation avec validation des règles métier
     * Accessible par : tous les utilisateurs authentifiés
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        
        try {
            ReservationResponse reservationResponse = reservationService.createReservation(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(reservationResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (InvalidReservationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(e.getMessage()));
        } catch (ReservationConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la création de la réservation"));
        }
    }
    
    /**
     * GET /api/reservations/my
     * Lister les réservations de l'utilisateur connecté
     * Accessible par : tous les utilisateurs authentifiés
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<ReservationResponse>> getMyReservations() {
        
        try {
            List<ReservationResponse> reservations = reservationService.getMyReservations();
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * GET /api/reservations
     * Lister toutes les réservations avec pagination
     * Accessible par : MANAGER, ADMIN
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Page<ReservationResponse>> getAllReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        
        try {
            Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
            
            Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
            Page<ReservationResponse> reservations = reservationService.getAllReservations(pageable);
            
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * GET /api/reservations/resource/{resourceId}
     * Consulter les réservations d'une ressource (calendrier)
     * Accessible par : tous les utilisateurs authentifiés
     */
    @GetMapping("/resource/{resourceId}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> getReservationsByResource(@PathVariable Long resourceId) {
        
        try {
            List<ReservationResponse> reservations = reservationService.getReservationsByResource(resourceId);
            return ResponseEntity.ok(reservations);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la récupération des réservations"));
        }
    }
    
    /**
     * GET /api/reservations/{id}
     * Consulter le détail d'une réservation
     * Accessible par : tous les utilisateurs authentifiés
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> getReservationById(@PathVariable Long id) {
        
        try {
            ReservationResponse reservationResponse = reservationService.getReservationById(id);
            return ResponseEntity.ok(reservationResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la récupération de la réservation"));
        }
    }
    
    /**
     * PUT /api/reservations/{id}
     * Modifier une réservation
     * Accessible par : propriétaire ou MANAGER
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationRequest request
    ) {
        
        try {
            ReservationResponse reservationResponse = reservationService.updateReservation(id, request);
            return ResponseEntity.ok(reservationResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse(e.getMessage()));
        } catch (InvalidReservationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(e.getMessage()));
        } catch (ReservationConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la mise à jour de la réservation"));
        }
    }
    
    /**
     * PATCH /api/reservations/{id}/cancel
     * Annuler une réservation
     * Accessible par : propriétaire ou MANAGER
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        
        try {
            ReservationResponse reservationResponse = reservationService.cancelReservation(id);
            return ResponseEntity.ok(reservationResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse(e.getMessage()));
        } catch (InvalidReservationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de l'annulation de la réservation"));
        }
    }
    
    /**
     * PATCH /api/reservations/{id}/confirm
     * Confirmer une réservation (MANAGER uniquement)
     * Accessible par : MANAGER, ADMIN
     */
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<?> confirmReservation(@PathVariable Long id) {
        
        try {
            ReservationResponse reservationResponse = reservationService.confirmReservation(id);
            return ResponseEntity.ok(reservationResponse);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(e.getMessage()));
        } catch (InvalidReservationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Erreur lors de la confirmation de la réservation"));
        }
    }
}