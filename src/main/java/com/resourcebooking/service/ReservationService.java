package com.resourcebooking.service;

import com.resourcebooking.dto.reservation.ReservationRequest;
import com.resourcebooking.dto.reservation.ReservationResponse;
import com.resourcebooking.entity.Reservation;
import com.resourcebooking.entity.Resource;
import com.resourcebooking.entity.User;
import com.resourcebooking.enums.ReservationStatus;
import com.resourcebooking.exception.*;
import com.resourcebooking.repository.ReservationRepository;
import com.resourcebooking.repository.ResourceRepository;
import com.resourcebooking.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.resourcebooking.enums.ReservationStatus;
import com.resourcebooking.specification.ReservationSpecification;
import com.resourcebooking.dto.reservation.ReservationUpdateRequest;
import org.springframework.security.access.AccessDeniedException;
import java.math.BigDecimal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Transactional
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ResourceRepository resourceRepository,
            UserRepository userRepository) {

        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    public ReservationResponse createReservation(
            ReservationRequest request,
            String username) {

        validateReservationTime(
                request.startTime(),
                request.endTime()
        );

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        )
                );

        Resource resource = resourceRepository.findById(
                        request.resourceId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: "
                                        + request.resourceId()
                        )
                );

        if (!Boolean.TRUE.equals(resource.getAvailable())) {
            throw new DuplicateResourceException(
                    "Resource is currently unavailable"
            );
        }

        boolean overlapping =
                reservationRepository.existsOverlappingReservation(
                        request.resourceId(),
                        request.startTime(),
                        request.endTime()
                );

        if (overlapping) {
            throw new DuplicateResourceException(
                    "Resource is already booked for the selected time"
            );
        }

        BigDecimal price = calculatePrice(
                resource.getPrice(),
                request.startTime(),
                request.endTime()
        );

        Reservation reservation = new Reservation();

        reservation.setUser(user);
        reservation.setResource(resource);
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(request.endTime());


        reservation.setPrice(price);


        reservation.setStatus(ReservationStatus.PENDING);

        Reservation savedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(savedReservation);
    }

    private BigDecimal calculatePrice(
            BigDecimal hourlyRate,
            LocalDateTime startTime,
            LocalDateTime endTime) {

        long minutes = Duration.between(
                startTime,
                endTime
        ).toMinutes();

        BigDecimal hours = BigDecimal.valueOf(minutes)
                .divide(
                        BigDecimal.valueOf(60),
                        4,
                        RoundingMode.HALF_UP
                );

        return hourlyRate
                .multiply(hours)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateReservationTime(
            LocalDateTime startTime,
            LocalDateTime endTime) {

        if (!startTime.isBefore(endTime)) {
            throw new InvalidReservationTimeException(
                    "Start time must be before end time"
            );
        }
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservationById(
            Long reservationId,
            String username,
            boolean isAdmin) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reservation not found with id: "
                                                + reservationId
                                )
                        );


        if (isAdmin) {
            return mapToResponse(reservation);
        }


        if (!reservation.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException(
                    "You are not authorized to access this reservation"
            );
        }

        return mapToResponse(reservation);
    }

    @Transactional(readOnly = true)
    public Page<ReservationResponse> getReservations(
            ReservationStatus status,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            int page,
            int size,
            String sortBy,
            String sortDirection,
            String username,
            boolean isAdmin) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page must be greater than or equal to 0"
            );
        }

        if (size <= 0) {
            throw new IllegalArgumentException(
                    "Size must be greater than 0"
            );
        }

        if (minPrice != null
                && maxPrice != null
                && minPrice.compareTo(maxPrice) > 0) {

            throw new IllegalArgumentException(
                    "Minimum price cannot be greater than maximum price"
            );
        }

        Sort sort = buildSort(sortBy, sortDirection);

        Pageable pageable =
                PageRequest.of(page, size, sort);

        Specification<Reservation> specification =
                Specification.where(
                        ReservationSpecification.hasStatus(status)
                );

        specification = specification.and(
                ReservationSpecification.priceGreaterThanOrEqualTo(
                        minPrice
                )
        );

        specification = specification.and(
                ReservationSpecification.priceLessThanOrEqualTo(
                        maxPrice
                )
        );


        if (!isAdmin) {

            specification = specification.and(
                    ReservationSpecification.belongsToUser(username)
            );
        }

        Page<Reservation> reservations =
                reservationRepository.findAll(
                        specification,
                        pageable
                );

        return reservations.map(this::mapToResponse);
    }

    private Sort buildSort(
            String sortBy,
            String sortDirection) {

        if (sortBy == null || sortBy.isBlank()) {
            return Sort.by("createdAt").descending();
        }

        Sort.Direction direction =
                "asc".equalsIgnoreCase(sortDirection)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        return Sort.by(direction, sortBy);
    }

    public ReservationResponse updateReservation(
            Long reservationId,
            ReservationUpdateRequest request,
            String username,
            boolean isAdmin) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reservation not found with id: "
                                                + reservationId
                                )
                        );


        if (!isAdmin &&
                !reservation.getUser()
                        .getUsername()
                        .equals(username)) {

            throw new AccessDeniedException(
                    "You are not authorized to update this reservation"
            );
        }

        validateReservationTime(
                request.startTime(),
                request.endTime()
        );

        boolean overlapping =
                reservationRepository
                        .existsOverlappingReservationExcludingId(
                                reservation.getResource().getId(),
                                request.startTime(),
                                request.endTime(),
                                reservationId
                        );

        if (overlapping) {
            throw new DuplicateResourceException(
                    "Resource is already booked for the selected time"
            );
        }

        Resource resource = reservation.getResource();

        if (!Boolean.TRUE.equals(resource.getAvailable())) {
            throw new DuplicateResourceException(
                    "Resource is currently unavailable"
            );
        }

        BigDecimal price = calculatePrice(
                resource.getPrice(),
                request.startTime(),
                request.endTime()
        );

        reservation.setStartTime(request.startTime());
        reservation.setEndTime(request.endTime());
        reservation.setPrice(price);

        Reservation updatedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(updatedReservation);
    }

    public void deleteReservation(
            Long reservationId,
            String username,
            boolean isAdmin) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reservation not found with id: "
                                                + reservationId
                                )
                        );


        if (isAdmin) {
            reservationRepository.delete(reservation);
            return;
        }

        if (!reservation.getUser()
                .getUsername()
                .equals(username)) {

            throw new AccessDeniedException(
                    "You are not authorized to cancel this reservation"
            );
        }


        if (reservation.getStatus() ==
                ReservationStatus.CANCELLED) {

            throw new DuplicateResourceException(
                    "Reservation is already cancelled"
            );
        }


        reservation.setStatus(
                ReservationStatus.CANCELLED
        );

        reservationRepository.save(reservation);
    }

    @Transactional
    public ReservationResponse updateReservationStatus(
            Long reservationId,
            ReservationStatus newStatus) {

        Reservation reservation =
                reservationRepository.findById(reservationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Reservation not found with id: "
                                                + reservationId
                                )
                        );

        ReservationStatus currentStatus =
                reservation.getStatus();

        validateStatusTransition(
                currentStatus,
                newStatus
        );

        reservation.setStatus(newStatus);

        Reservation updatedReservation =
                reservationRepository.save(reservation);

        return mapToResponse(updatedReservation);
    }

    private void validateStatusTransition(
            ReservationStatus currentStatus,
            ReservationStatus newStatus) {

        if (currentStatus == newStatus) {
            throw new ReservationConflictException(
                    "Reservation is already in "
                            + currentStatus
                            + " status"
            );
        }

        boolean validTransition =
                switch (currentStatus) {

                    case PENDING ->
                            newStatus == ReservationStatus.CONFIRMED
                                    || newStatus == ReservationStatus.CANCELLED;

                    case CONFIRMED ->
                            newStatus == ReservationStatus.CANCELLED;

                    case CANCELLED ->
                            false;
                };

        if (!validTransition) {
            throw new ReservationConflictException(
                    "Invalid reservation status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }
    }
    private ReservationResponse mapToResponse(
            Reservation reservation) {

        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getId(),
                reservation.getUser().getUsername(),
                reservation.getResource().getId(),
                reservation.getResource().getName(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getPrice(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}