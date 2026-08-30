package com.resourcebooking.controller;

import com.resourcebooking.common.response.ApiResponse;
import com.resourcebooking.dto.resource.ResourceRequest;
import com.resourcebooking.dto.resource.ResourceResponse;
import com.resourcebooking.dto.resource.ResourceUpdateRequest;
import com.resourcebooking.service.ResourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@Tag(
        name = "Resources",
        description = "APIs for managing booking resources"
)
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }


    @Operation(
            summary = "Create resource",
            description = "Creates a new booking resource. ADMIN only."
    )
    @ApiResponses({
             @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Resource created successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid resource data"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Resource with the same name already exists"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied. ADMIN role required."
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ResourceResponse>> createResource(
            @Valid @RequestBody ResourceRequest request) {

        ResourceResponse response =
                resourceService.createResource(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Resource created successfully",
                                response
                        )
                );
    }

    @Operation(
            summary = "Get all resources",
            description = "Returns all available resources"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Resources retrieved successfully"
    )
    @GetMapping
//  @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<List<ResourceResponse>>> getAllResources() {

        List<ResourceResponse> resources =
                resourceService.getAllResources();

        return ResponseEntity.ok(
                ApiResponse.success(
                        " All Resources retrieved successfully",
                        resources
                )
        );
    }



    @Operation(
            summary = "Get resource by ID",
            description = "Returns details of a specific resource"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Resource found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Resource not found"
            )
    })
    @GetMapping("/{id}")
// @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<ResourceResponse>> getResourceById(
            @Parameter(
                    description = "ID of the resource",
                    example = "1"
            )
            @PathVariable Long id
            ) {

        ResourceResponse resource =
                resourceService.getResourceById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Resource retrieved successfully",
                        resource
                )
        );
    }


    @Operation(
            summary = "Update resource",
            description = "Updates an existing resource. ADMIN only."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Resource updated successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Resource not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Resource name already exists"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied. ADMIN role required."
            )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ResourceResponse>> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceUpdateRequest request) {

        ResourceResponse resource =
                resourceService.updateResource(id, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Resource updated successfully",
                        resource
                )
        );
    }


    @Operation(
            summary = "Delete resource",
            description = "Deletes a resource if it has no reservations. ADMIN only."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "204",
                    description = "Resource deleted successfully"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Resource not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Resource cannot be deleted because reservations exist"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "Access denied. ADMIN role required."
            )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteResource(
            @PathVariable Long id) {

        resourceService.deleteResource(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Resource deleted successfully",
                        null
                )
        );
    }

}