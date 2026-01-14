package com.reservation.enums;

public enum ReservationStatus {
    PENDING,      // En attente de confirmation
    CONFIRMED,    // Confirmée par un MANAGER
    CANCELLED,    // Annulée
    COMPLETED     // Terminée (passée)
}