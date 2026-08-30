package com.resourcebooking.repository;

import com.resourcebooking.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long>,
        JpaSpecificationExecutor<Reservation> {

    List<Reservation> findByUserId(Long userId);
    boolean existsByResourceId(Long resourceId);
    boolean existsByResourceIdAndStartTimeLessThanAndEndTimeGreaterThan(
            Long resourceId,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    @Query("""
            SELECT COUNT(r) > 0
            FROM Reservation r
            WHERE r.resource.id = :resourceId
              AND r.status <> com.resourcebooking.enums.ReservationStatus.CANCELLED
              AND r.startTime < :endTime
              AND r.endTime > :startTime
            """)
    boolean existsOverlappingReservation(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
    @Query("""
        SELECT COUNT(r) > 0
        FROM Reservation r
        WHERE r.id <> :reservationId
          AND r.resource.id = :resourceId
          AND r.status <> com.resourcebooking.enums.ReservationStatus.CANCELLED
          AND r.startTime < :endTime
          AND r.endTime > :startTime
        """)
    boolean existsOverlappingReservationForUpdate(
            @Param("reservationId") Long reservationId,
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
    @Query("""
        SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
        FROM Reservation r
        WHERE r.resource.id = :resourceId
          AND r.id <> :reservationId
          AND r.status <> com.resourcebooking.enums.ReservationStatus.CANCELLED
          AND r.startTime < :endTime
          AND r.endTime > :startTime
        """)
    boolean existsOverlappingReservationExcludingId(
            @Param("resourceId") Long resourceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("reservationId") Long reservationId
    );
}