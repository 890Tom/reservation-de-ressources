package com.reservation.service;

import com.reservation.dto.request.CreateReservationRequest;
import com.reservation.dto.request.UpdateReservationRequest;
import com.reservation.dto.response.ReservationResponse;
import com.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReservationService {
    
    /**
     * Créer une réservation avec validation des règles métier
     */
    ReservationResponse createReservation(CreateReservationRequest request);
    
    /**
     * Lister les réservations de l'utilisateur connecté
     */
    List<ReservationResponse> getMyReservations();
    
    /**
     * Lister toutes les réservations (MANAGER/ADMIN)
     */
    Page<ReservationResponse> getAllReservations(Pageable pageable);
    
    /**
     * Consulter les réservations d'une ressource
     */
    List<ReservationResponse> getReservationsByResource(Long resourceId);
    
    /**
     * Consulter le détail d'une réservation
     */
    ReservationResponse getReservationById(Long id);
    
    /**
     * Modifier une réservation (propriétaire ou MANAGER)
     */
    ReservationResponse updateReservation(Long id, UpdateReservationRequest request);
    
    /**
     * Annuler une réservation
     */
    ReservationResponse cancelReservation(Long id);
    
    /**
     * Confirmer une réservation (MANAGER uniquement)
     */
    ReservationResponse confirmReservation(Long id);
    
    /**
     * Convertir Reservation en ReservationResponse
     */
    ReservationResponse toReservationResponse(Reservation reservation);
}