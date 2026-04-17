package com.hospital.service;

import com.hospital.model.MedicalResource;
import com.hospital.model.Patient;
import com.hospital.pattern.ResourceAllocatorDecorator;
import com.hospital.repository.MedicalResourceRepository;
import com.hospital.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Uses the Decorator pattern to perform allocation with validation + logging.
 */
@Service
@RequiredArgsConstructor
public class ResourceService {

    private final MedicalResourceRepository resourceRepository;
    private final PatientRepository patientRepository;

    // Fully decorated allocator: Validation -> Logging -> Base
    private final ResourceAllocatorDecorator.ResourceAllocator allocator =
        ResourceAllocatorDecorator.createFullyDecoratedAllocator();

    public List<MedicalResource> getAllResources() {
        return resourceRepository.findAll();
    }

    public Optional<MedicalResource> getResourceById(Long id) {
        return resourceRepository.findById(id);
    }

    public List<MedicalResource> getByType(MedicalResource.ResourceType type) {
        return resourceRepository.findByType(type);
    }

    public List<MedicalResource> getAvailableResources() {
        return resourceRepository.findByAvailableQuantityGreaterThan(0);
    }

    @Transactional
    public MedicalResource saveResource(MedicalResource resource) {
        return resourceRepository.save(resource);
    }

    /**
     * Allocate resource to patient using the Decorator chain.
     */
    @Transactional
    public boolean allocateResource(Long resourceId, Long patientId) {
        MedicalResource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new RuntimeException("Resource not found: " + resourceId));
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));

        boolean success = allocator.allocate(resource, patient);
        if (success) resourceRepository.save(resource);
        return success;
    }

    /**
     * Release resource back to available pool.
     */
    @Transactional
    public boolean releaseResource(Long resourceId) {
        MedicalResource resource = resourceRepository.findById(resourceId)
            .orElseThrow(() -> new RuntimeException("Resource not found: " + resourceId));

        boolean success = allocator.release(resource);
        if (success) resourceRepository.save(resource);
        return success;
    }

    @Transactional
    public void deleteResource(Long id) {
        resourceRepository.deleteById(id);
    }

    public long countAvailable() {
        return resourceRepository.countByAvailabilityStatus(MedicalResource.AvailabilityStatus.AVAILABLE);
    }

    public long countTotal() {
        return resourceRepository.count();
    }
}
