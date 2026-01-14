package com.reservation.entity;

import com.reservation.enums.ReservationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "L'utilisateur est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotNull(message = "La ressource est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;
    
    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    @Column(nullable = false)
    private LocalDateTime startTime;
    
    @NotNull(message = "La date de fin est obligatoire")
    @Future(message = "La date de fin doit être dans le futur")
    @Column(nullable = false)
    private LocalDateTime endTime;
    
    @Column(length = 1000)
    private String purpose;
    
    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Méthode utilitaire pour vérifier si la réservation peut être annulée
    // Règle : Annulation possible jusqu'à 2h avant le début
    public boolean canBeCancelled() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancellationDeadline = startTime.minusHours(2);
        return now.isBefore(cancellationDeadline) && 
               (status == ReservationStatus.PENDING || status == ReservationStatus.CONFIRMED);
    }
    
    // Méthode pour vérifier si les dates sont valides
    // Règle : Durée Min 30 min, Max 8 heures
    public boolean hasValidDuration() {
        long durationMinutes = java.time.Duration.between(startTime, endTime).toMinutes();
        return durationMinutes >= 30 && durationMinutes <= 480; // 8 heures = 480 minutes
    }
    
    // Méthode pour vérifier l'anticipation
    // Règle : Réservation jusqu'à 30 jours à l'avance
    public boolean isWithinBookingWindow() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxBookingDate = now.plusDays(30);
        return startTime.isAfter(now) && startTime.isBefore(maxBookingDate);
    }
}