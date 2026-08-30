package com.resourcebooking.repository;

import com.resourcebooking.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
}