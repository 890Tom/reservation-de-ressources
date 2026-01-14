package com.reservation.service.impl;

import com.reservation.dto.request.CreateReservationRequest;
import com.reservation.dto.request.UpdateReservationRequest;
import com.reservation.dto.response.ReservationResponse;
import com.reservation.entity.Reservation;
import com.reservation.entity.Resource;
import com.reservation.entity.User;
import com.reservation.enums.ReservationStatus;
import com.reservation.enums.Role;
import com.reservation.exception.InvalidReservationException;
import com.reservation.exception.ReservationConflictException;
import com.reservation.exception.ResourceNotFoundException;
import com.reservation.exception.UnauthorizedException;
import com.reservation.repository.ReservationRepository;
import com.reservation.repository.ResourceRepository;
import com.reservation.repository.UserRepository;
import com.reservation.security.service.UserDetailsImpl;
import com.reservation.service.ReservationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {
    
    private static final long MIN_DURATION_MINUTES = 30;
    private static final long MAX_DURATION_HOURS = 8;
    private static final long MAX_ADVANCE_DAYS = 30;
    private static final long MIN_CANCELLATION_HOURS = 2;
    
    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    
    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                 ResourceRepository resourceRepository,
                                 UserRepository userRepository) {
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public ReservationResponse createReservation(CreateReservationRequest request) {
        
        User currentUser = getCurrentUser();
        
        // Vérifier que la ressource existe
        Resource resource = resourceRepository.findById(request.getResourceId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Ressource non trouvée avec l'ID: " + request.getResourceId()));
        
        // RÈGLE 1 : Vérifier que startTime < endTime
        if (!request.getStartTime().isBefore(request.getEndTime())) {
            throw new InvalidReservationException("La date de début doit être avant la date de fin");
        }
        
        // RÈGLE 2 : Durée minimale (30 min) et maximale (8h)
        long durationMinutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (durationMinutes < MIN_DURATION_MINUTES) {
            throw new InvalidReservationException(
                "La durée de réservation doit être d'au moins " + MIN_DURATION_MINUTES + " minutes");
        }
        if (durationMinutes > MAX_DURATION_HOURS * 60) {
            throw new InvalidReservationException(
                "La durée de réservation ne peut pas dépasser " + MAX_DURATION_HOURS + " heures");
        }
        
        // RÈGLE 3 : Anticipation maximale (30 jours)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxBookingDate = now.plusDays(MAX_ADVANCE_DAYS);
        if (request.getStartTime().isAfter(maxBookingDate)) {
            throw new InvalidReservationException(
                "Les réservations ne peuvent être faites que jusqu'à " + MAX_ADVANCE_DAYS + " jours à l'avance");
        }
        
        // RÈGLE 4 : Anti-conflit - vérifier qu'il n'y a pas de réservations qui se chevauchent
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
            request.getResourceId(), request.getStartTime(), request.getEndTime());
        
        if (!conflicts.isEmpty()) {
            throw new ReservationConflictException(
                "La ressource n'est pas disponible sur ce créneau. " + conflicts.size() + " conflit(s) détecté(s).");
        }
        
        // Créer la réservation
        Reservation reservation = new Reservation();
        reservation.setUser(currentUser);
        reservation.setResource(resource);
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setPurpose(request.getPurpose());
        reservation.setStatus(ReservationStatus.PENDING);
        
        Reservation savedReservation = reservationRepository.save(reservation);
        
        return toReservationResponse(savedReservation);
    }
    
    @Override
    public List<ReservationResponse> getMyReservations() {
        User currentUser = getCurrentUser();
        
        List<Reservation> reservations = reservationRepository.findByUserId(currentUser.getId());
        return reservations.stream()
            .map(this::toReservationResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public Page<ReservationResponse> getAllReservations(Pageable pageable) {
        return reservationRepository.findAll(pageable).map(this::toReservationResponse);
    }
    
    @Override
    public List<ReservationResponse> getReservationsByResource(Long resourceId) {
        
        // Vérifier que la ressource existe
        resourceRepository.findById(resourceId)
            .orElseThrow(() -> new ResourceNotFoundException("Ressource non trouvée avec l'ID: " + resourceId));
        
        List<Reservation> reservations = reservationRepository.findByResourceId(resourceId);
        return reservations.stream()
            .map(this::toReservationResponse)
            .collect(Collectors.toList());
    }
    
    @Override
    public ReservationResponse getReservationById(Long id) {
        
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée avec l'ID: " + id));
        
        return toReservationResponse(reservation);
    }
    
    @Override
    public ReservationResponse updateReservation(Long id, UpdateReservationRequest request) {
        
        User currentUser = getCurrentUser();
        
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée avec l'ID: " + id));
        
        // Vérifier les permissions (propriétaire ou MANAGER)
        boolean isOwner = reservation.getUser().getId().equals(currentUser.getId());
        boolean isManager = currentUser.getRole() == Role.MANAGER || currentUser.getRole() == Role.ADMIN;
        
        if (!isOwner && !isManager) {
            throw new UnauthorizedException("Vous n'avez pas la permission de modifier cette réservation");
        }
        
        // Si les dates sont modifiées, valider les règles
        LocalDateTime newStartTime = request.getStartTime() != null ? request.getStartTime() : reservation.getStartTime();
        LocalDateTime newEndTime = request.getEndTime() != null ? request.getEndTime() : reservation.getEndTime();
        
        // Vérifier la cohérence des dates
        if (!newStartTime.isBefore(newEndTime)) {
            throw new InvalidReservationException("La date de début doit être avant la date de fin");
        }
        
        // Vérifier la durée
        long durationMinutes = Duration.between(newStartTime, newEndTime).toMinutes();
        if (durationMinutes < MIN_DURATION_MINUTES || durationMinutes > MAX_DURATION_HOURS * 60) {
            throw new InvalidReservationException(
                "La durée doit être entre " + MIN_DURATION_MINUTES + " minutes et " + MAX_DURATION_HOURS + " heures");
        }
        
        // Vérifier l'anticipation
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxBookingDate = now.plusDays(MAX_ADVANCE_DAYS);
        if (newStartTime.isAfter(maxBookingDate)) {
            throw new InvalidReservationException(
                "Les réservations ne peuvent être faites que jusqu'à " + MAX_ADVANCE_DAYS + " jours à l'avance");
        }
        
        // Vérifier les conflits (exclure la réservation actuelle)
        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
            reservation.getResource().getId(), newStartTime, newEndTime);
        
        conflicts = conflicts.stream()
            .filter(r -> !r.getId().equals(id))
            .collect(Collectors.toList());
        
        if (!conflicts.isEmpty()) {
            throw new ReservationConflictException(
                "La ressource n'est pas disponible sur ce nouveau créneau");
        }
        
        // Mettre à jour les champs
        if (request.getStartTime() != null) {
            reservation.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            reservation.setEndTime(request.getEndTime());
        }
        if (request.getPurpose() != null) {
            reservation.setPurpose(request.getPurpose());
        }
        
        Reservation updatedReservation = reservationRepository.save(reservation);
        
        return toReservationResponse(updatedReservation);
    }
    
    @Override
    public ReservationResponse cancelReservation(Long id) {
        
        User currentUser = getCurrentUser();
        
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée avec l'ID: " + id));
        
        // Vérifier les permissions (propriétaire ou MANAGER)
        boolean isOwner = reservation.getUser().getId().equals(currentUser.getId());
        boolean isManager = currentUser.getRole() == Role.MANAGER || currentUser.getRole() == Role.ADMIN;
        
        if (!isOwner && !isManager) {
            throw new UnauthorizedException("Vous n'avez pas la permission d'annuler cette réservation");
        }
        
        // RÈGLE : Annulation possible jusqu'à 2h avant le début
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancellationDeadline = reservation.getStartTime().minusHours(MIN_CANCELLATION_HOURS);
        
        if (now.isAfter(cancellationDeadline)) {
            throw new InvalidReservationException(
                "L'annulation n'est possible que jusqu'à " + MIN_CANCELLATION_HOURS + " heures avant le début de la réservation");
        }
        
        // Vérifier que la réservation n'est pas déjà annulée
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new InvalidReservationException("Cette réservation est déjà annulée");
        }
        
        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation cancelledReservation = reservationRepository.save(reservation);
        
        return toReservationResponse(cancelledReservation);
    }
    
    @Override
    public ReservationResponse confirmReservation(Long id) {
        
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée avec l'ID: " + id));
        
        // Vérifier que la réservation est en attente
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new InvalidReservationException("Seules les réservations en attente peuvent être confirmées");
        }
        
        reservation.setStatus(ReservationStatus.CONFIRMED);
        Reservation confirmedReservation = reservationRepository.save(reservation);
        
        return toReservationResponse(confirmedReservation);
    }
    
    @Override
    public ReservationResponse toReservationResponse(Reservation reservation) {
        ReservationResponse response = new ReservationResponse();
        response.setId(reservation.getId());
        response.setUserId(reservation.getUser().getId());
        response.setUsername(reservation.getUser().getUsername());
        response.setResourceId(reservation.getResource().getId());
        response.setResourceName(reservation.getResource().getName());
        response.setStartTime(reservation.getStartTime());
        response.setEndTime(reservation.getEndTime());
        response.setPurpose(reservation.getPurpose());
        response.setStatus(reservation.getStatus());
        response.setCreatedAt(reservation.getCreatedAt());
        response.setUpdatedAt(reservation.getUpdatedAt());
        return response;
    }
    
    /**
     * Récupère l'utilisateur actuellement connecté
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return userRepository.findByUsername(userDetails.getUsername())
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
    }
}