package com.reservation.repository;

import com.reservation.entity.Resource;
import com.reservation.enums.ResourceStatus;
import com.reservation.enums.ResourceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long> {
    
    Page<Resource> findByType(ResourceType type, Pageable pageable);
    
    Page<Resource> findByStatus(ResourceStatus status, Pageable pageable);
    
    Page<Resource> findByTypeAndStatus(ResourceType type, ResourceStatus status, Pageable pageable);
    
    List<Resource> findByStatusOrderByNameAsc(ResourceStatus status);
    
    // Vérifier la disponibilité d'une ressource sur un créneau
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN false ELSE true END " +
           "FROM Reservation r " +
           "WHERE r.resource.id = :resourceId " +
           "AND r.status IN ('PENDING', 'CONFIRMED') " +
           "AND ((r.startTime < :endTime AND r.endTime > :startTime))")
    boolean isResourceAvailable(@Param("resourceId") Long resourceId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);
}