package com.hospital.service;

import com.hospital.model.Bed;
import com.hospital.pattern.BedAllocatorFactory;
import com.hospital.pattern.BedStatusObserver;
import com.hospital.repository.BedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * DESIGN PRINCIPLE: Single Responsibility Principle (SRP)
 * BedService handles ONLY bed management logic.
 *
 * DESIGN PRINCIPLE: Open/Closed Principle (OCP)
 * New ward types or allocation strategies can be added via the Factory
 * without modifying this service.
 */
@Service
@RequiredArgsConstructor
public class BedService {

    private final BedRepository bedRepository;
    private final BedAllocatorFactory bedAllocatorFactory;
    private final BedStatusObserver.BedStatusEventPublisher eventPublisher;

    public List<Bed> getAllBeds() {
        return bedRepository.findAll();
    }

    public Optional<Bed> getBedById(Long id) {
        return bedRepository.findById(id);
    }

    public List<Bed> getAvailableBeds() {
        return bedRepository.findByStatus(Bed.BedStatus.AVAILABLE);
    }

    public List<Bed> getAvailableBedsByWard(Bed.WardType wardType) {
        BedAllocatorFactory.BedAllocator allocator = bedAllocatorFactory.getAllocator(wardType);
        return allocator.findSuitableBeds(wardType);
    }

    public List<Bed> getBedsByWard(Bed.WardType wardType) {
        return bedRepository.findByWardType(wardType);
    }

    @Transactional
    public Bed saveBed(Bed bed) {
        return bedRepository.save(bed);
    }

    @Transactional
    public Bed updateBedStatus(Long bedId, Bed.BedStatus newStatus) {
        Bed bed = bedRepository.findById(bedId)
            .orElseThrow(() -> new RuntimeException("Bed not found: " + bedId));

        Bed.BedStatus previousStatus = bed.getStatus();
        bed.setStatus(newStatus);
        Bed saved = bedRepository.save(bed);

        // Observer pattern: notify all listeners of status change
        eventPublisher.publishStatusChange(this, saved, previousStatus, newStatus);

        return saved;
    }

    @Transactional
    public void deleteBed(Long bedId) {
        bedRepository.deleteById(bedId);
    }

    public long countAvailable() {
        return bedRepository.countByStatus(Bed.BedStatus.AVAILABLE);
    }

    public long countOccupied() {
        return bedRepository.countByStatus(Bed.BedStatus.OCCUPIED);
    }

    public long countTotal() {
        return bedRepository.count();
    }
}
