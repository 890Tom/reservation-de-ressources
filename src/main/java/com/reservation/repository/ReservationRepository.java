package com.reservation.repository;

import com.reservation.entity.Reservation;
import com.reservation.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    List<Reservation> findByUserId(Long userId);
    
    Page<Reservation> findByUserId(Long userId, Pageable pageable);
    
    List<Reservation> findByResourceId(Long resourceId);
    
    Page<Reservation> findByResourceId(Long resourceId, Pageable pageable);
    
    List<Reservation> findByStatus(ReservationStatus status);
    
    Page<Reservation> findByStatus(ReservationStatus status, Pageable pageable);
    
    // Trouver les réservations d'une ressource dans un intervalle de temps
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.resource.id = :resourceId " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND r.startTime < :endTime " +
           "AND r.endTime > :startTime")
    List<Reservation> findConflictingReservations(@Param("resourceId") Long resourceId,
                                                   @Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);
    
    // Trouver les réservations futures d'un utilisateur
    @Query("SELECT r FROM Reservation r " +
           "WHERE r.user.id = :userId " +
           "AND r.startTime > :now " +
           "ORDER BY r.startTime ASC")
    List<Reservation> findUpcomingReservationsByUserId(@Param("userId") Long userId,
                                                        @Param("now") LocalDateTime now);
    
    // Compter les réservations actives d'une ressource
    @Query("SELECT COUNT(r) FROM Reservation r " +
           "WHERE r.resource.id = :resourceId " +
           "AND r.status IN ('PENDING', 'CONFIRMED')")
    Long countActiveReservationsByResourceId(@Param("resourceId") Long resourceId);
}