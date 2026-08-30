package com.resourcebooking.dto.reservation;

import com.resourcebooking.enums.ReservationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationResponse(

        Long id,

        Long userId,

        String username,

        Long resourceId,

        String resourceName,

        LocalDateTime startTime,

        LocalDateTime endTime,

        BigDecimal price,

        ReservationStatus status,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}