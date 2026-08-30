package com.resourcebooking.dto.resource;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ResourceUpdateRequest(

        @NotBlank(message = "Resource name is required")
        String name,

        String description,

        @NotBlank(message = "Location is required")
        String location,

        @NotNull(message = "Capacity is required")
        @Positive(message = "Capacity must be greater than zero")
        Integer capacity,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        BigDecimal price,

        @NotNull(message = "Availability is required")
        Boolean available
) {
}