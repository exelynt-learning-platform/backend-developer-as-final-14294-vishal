package com.resourcebooking.service;

import com.resourcebooking.dto.resource.ResourceRequest;
import com.resourcebooking.dto.resource.ResourceResponse;
import com.resourcebooking.dto.resource.ResourceUpdateRequest;
import com.resourcebooking.entity.Resource;
import com.resourcebooking.exception.DuplicateResourceException;
import com.resourcebooking.exception.ReservationConflictException;
import com.resourcebooking.exception.ResourceConflictException;
import com.resourcebooking.exception.ResourceNotFoundException;
import com.resourcebooking.repository.ReservationRepository;
import com.resourcebooking.repository.ResourceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;

    public ResourceService(ResourceRepository resourceRepository, ReservationRepository reservationRepository) {
        this.resourceRepository = resourceRepository;
        this.reservationRepository = reservationRepository;
    }

    public ResourceResponse createResource(ResourceRequest request) {

        if (resourceRepository.existsByName(request.name())) {
            throw new DuplicateResourceException(
                    "Resource already exists with name: " + request.name()
            );
        }

        Resource resource = new Resource();

        resource.setName(request.name());
        resource.setDescription(request.description());
        resource.setLocation(request.location());
        resource.setCapacity(request.capacity());
        resource.setPrice(request.price());
        resource.setAvailable(request.available());

        Resource savedResource = resourceRepository.save(resource);

        return mapToResponse(savedResource);
    }

    public List<ResourceResponse> getAllResources() {

        return resourceRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ResourceResponse getResourceById(Long id) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: " + id
                        )
                );

        return mapToResponse(resource);
    }

    public ResourceResponse updateResource(
            Long id,
            ResourceUpdateRequest request) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Resource not found with id: " + id
                        )
                );
        if (resourceRepository.existsByNameAndIdNot(
                request.name(),
                id
        )) {
            throw new DuplicateResourceException(
                    "Resource already exists with name: " + request.name()
            );
        }

        resource.setName(request.name());
        resource.setDescription(request.description());
        resource.setLocation(request.location());
        resource.setCapacity(request.capacity());
        resource.setPrice(request.price());
        resource.setAvailable(request.available());

        Resource updatedResource =
                resourceRepository.save(resource);

        return mapToResponse(updatedResource);
    }

    @Transactional
    public void deleteResource(Long id) {

        Resource resource =
                resourceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Resource not found with id: " + id
                                )
                        );

        boolean hasReservations =
                reservationRepository.existsByResourceId(id);

        if (hasReservations) {
            throw new ResourceConflictException(
                    "Resource cannot be deleted because reservations exist for it"
            );
        }

        resourceRepository.delete(resource);
    }

    private ResourceResponse mapToResponse(Resource resource) {

        return new ResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getLocation(),
                resource.getCapacity(),
                resource.getPrice(),
                resource.getAvailable(),
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }
}