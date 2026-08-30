package com.resourcebooking.controller;

import com.resourcebooking.common.response.ApiResponse;
import com.resourcebooking.dto.reservation.ReservationRequest;
import com.resourcebooking.dto.reservation.ReservationResponse;
import com.resourcebooking.dto.reservation.ReservationStatusUpdateRequest;
import com.resourcebooking.dto.reservation.ReservationUpdateRequest;
import com.resourcebooking.enums.ReservationStatus;
import com.resourcebooking.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/reservations")
@Tag(
        name = "Reservations",
        description = "APIs for creating and managing resource reservations"
)
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(
            ReservationService reservationService) {
        this.reservationService = reservationService;
    }


    @Operation(
            summary = "Create reservation",
            description = """
                Creates a reservation for the authenticated user.
                The user identity is taken from the JWT.
                Reservation price is calculated by the server using
                the selected resource price and reservation duration.
                """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Reservation created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid reservation data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Resource not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Resource unavailable or reservation time conflicts"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> createReservation(
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication) {

        String username = authentication.getName();

        ReservationResponse response =
                reservationService.createReservation(
                        request,
                        username
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Reservation created successfully",
                                response
                        )
                );
    }



    @Operation(
            summary = "Get reservation by ID",
            description = """
                Retrieves a reservation by its ID.
                ADMIN can view any reservation.
                USER can view only their own reservations.
                """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Reservation retrieved successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> getReservationById(
            @Parameter(
                    description = "ID of the reservation",
                    example = "1"
            )
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN")
                );

        ReservationResponse response =
                reservationService.getReservationById(
                        id,
                        username,
                        isAdmin
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Reservation retrieved successfully",
                        response
                )
        );
    }
    @Operation(
            summary = "Get reservations",
            description = """
                Returns reservations based on the authenticated user's role.
                ADMIN can view all reservations.
                USER can view only their own reservations.

                Supports filtering by status and price range,
                pagination using page and size, and optional sorting.
                """
    )
    @GetMapping
    public ResponseEntity<ApiResponse<Page<ReservationResponse>>> getReservations(
            @Parameter(
                    description = "Filter reservations by status",
                    example = "CONFIRMED"
            )
            @RequestParam(required = false)
            ReservationStatus status,

            @Parameter(
                    description = "Minimum reservation price",
                    example = "500.00"
            )
            @RequestParam(required = false)
            BigDecimal minPrice,

            @Parameter(
                    description = "Maximum reservation price",
                    example = "1000.00"
            )
            @RequestParam(required = false)
            BigDecimal maxPrice,

            @Parameter(
                    description = "Page number (0-based)",
                    example = "0"
            )
            @RequestParam(defaultValue = "0")
            int page,

            @Parameter(
                    description = "Number of items per page",
                    example = "10"
            )
            @RequestParam(defaultValue = "10")
            int size,

            @Parameter(
                    description = "Sorting field ",
                    example = "price"
            )
            @RequestParam(required = false)
            String sortBy,

            @Parameter(
                    description = "Sort direction",
                    example = "desc"
            )
            @RequestParam(defaultValue = "desc")
            String sortDirection,

            Authentication authentication) {

        String username = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN")
                );

        Page<ReservationResponse> response =
                reservationService.getReservations(
                        status,
                        minPrice,
                        maxPrice,
                        page,
                        size,
                        sortBy,
                        sortDirection,
                        username,
                        isAdmin
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Reservations retrieved successfully",
                        response
                )
        );
    }


    @Operation(
            summary = "Update reservation",
            description = """
                Updates the reservation time for the authenticated user.
                USER can update only their own reservation.
                ADMIN can update reservations according to the configured
                authorization rules.
                """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Reservation updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid reservation data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Reservation or resource not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Reservation time conflicts with another reservation"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request,
            Authentication authentication) {
        System.out.println("Enter into updateReservation method");
        String username = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN")
                );
        System.out.println("Role :" + isAdmin);
        ReservationResponse response =
                reservationService.updateReservation(
                        id,
                        request,
                        username,
                        isAdmin
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Reservation updated successfully",
                        response
                )
        );
    }

    @Operation(
            summary = "Cancel reservation",
            description = "Cancels a reservation. USER can cancel only their own reservation."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Reservation cancelled successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReservation(
            @PathVariable Long id,
            Authentication authentication) {

        String username = authentication.getName();

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_ADMIN")
                );

        reservationService.deleteReservation(
                id,
                username,
                isAdmin
        );

        String message = isAdmin
                ? "Reservation deleted successfully"
                : "Reservation cancelled successfully";

        return ResponseEntity.ok(
                ApiResponse.success(
                        message,
                        null
                )
        );
    }

    @Operation(
            summary = "Update reservation status",
            description = """
                Updates the status of an existing reservation.

                Supported statuses:
                PENDING, CONFIRMED, CANCELLED.

                This operation is restricted according to the configured
                reservation authorization rules.
                """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Reservation status updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid reservation status"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Reservation not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ReservationResponse>> updateReservationStatus(
            @Parameter(
                    description = "ID of the reservation",
                    example = "1"
            )
            @PathVariable Long id,
            @Valid @RequestBody ReservationStatusUpdateRequest request) {

        ReservationResponse response =
                reservationService.updateReservationStatus(
                        id,
                        request.status()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Reservation status updated successfully",
                        response
                )
        );
    }
}