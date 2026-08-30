package com.resourcebooking.specification;

import com.resourcebooking.entity.Reservation;
import com.resourcebooking.enums.ReservationStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ReservationSpecification {

    private ReservationSpecification() {
    }

    public static Specification<Reservation> hasStatus(
            ReservationStatus status) {

        return (root, query, criteriaBuilder) -> {

            if (status == null) {
                return null;
            }

            return criteriaBuilder.equal(
                    root.get("status"),
                    status
            );
        };
    }

    public static Specification<Reservation> priceGreaterThanOrEqualTo(
            BigDecimal minPrice) {

        return (root, query, criteriaBuilder) -> {

            if (minPrice == null) {
                return null;
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("price"),
                    minPrice
            );
        };
    }

    public static Specification<Reservation> priceLessThanOrEqualTo(
            BigDecimal maxPrice) {

        return (root, query, criteriaBuilder) -> {

            if (maxPrice == null) {
                return null;
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("price"),
                    maxPrice
            );
        };
    }

    public static Specification<Reservation> belongsToUser(
            String username) {

        return (root, query, criteriaBuilder) -> {

            if (username == null) {
                return null;
            }

            return criteriaBuilder.equal(
                    root.get("user").get("username"),
                    username
            );
        };
    }
}