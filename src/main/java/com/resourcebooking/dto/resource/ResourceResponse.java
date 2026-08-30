package com.resourcebooking.dto.resource;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ResourceResponse(

        Long id,
        String name,
        String description,
        String location,
        Integer capacity,
        BigDecimal price,
        Boolean available,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}