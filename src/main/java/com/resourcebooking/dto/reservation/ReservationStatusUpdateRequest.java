package com.resourcebooking.dto.reservation;

import com.resourcebooking.enums.ReservationStatus;
import jakarta.validation.constraints.NotNull;

public record ReservationStatusUpdateRequest(

        @NotNull(message = "Reservation status is required")
        ReservationStatus status

) {
}