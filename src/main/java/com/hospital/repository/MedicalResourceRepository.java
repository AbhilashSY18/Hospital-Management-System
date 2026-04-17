package com.hospital.repository;

import com.hospital.model.MedicalResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalResourceRepository extends JpaRepository<MedicalResource, Long> {

    List<MedicalResource> findByType(MedicalResource.ResourceType type);

    List<MedicalResource> findByAvailabilityStatus(MedicalResource.AvailabilityStatus status);

    List<MedicalResource> findByAvailableQuantityGreaterThan(int quantity);

    long countByAvailabilityStatus(MedicalResource.AvailabilityStatus status);
}
