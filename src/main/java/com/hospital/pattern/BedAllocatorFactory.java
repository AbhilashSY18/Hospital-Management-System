package com.hospital.pattern;

import com.hospital.model.Bed;
import com.hospital.repository.BedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * DESIGN PATTERN: Factory Method (Creational)
 *
 * Intent: Creates the appropriate BedAllocator object based on ward type,
 * without exposing instantiation logic to the caller.
 *
 * Application to domain: When admitting a patient, the system needs to find
 * the best available bed. The allocation strategy differs by ward type
 * (e.g., ICU beds are allocated strictly; general beds allow flexible matching).
 * The factory encapsulates this logic and returns the right allocator.
 */
@Component
public class BedAllocatorFactory {

    // ---- Allocator interface ----
    public interface BedAllocator {
        List<Bed> findSuitableBeds(Bed.WardType wardType);
        String getStrategyDescription();
    }

    private final BedRepository bedRepository;

    @Autowired
    public BedAllocatorFactory(BedRepository bedRepository) {
        this.bedRepository = bedRepository;
    }

    /**
     * Factory method: returns the correct BedAllocator for a given ward type.
     */
    public BedAllocator getAllocator(Bed.WardType wardType) {
        return switch (wardType) {
            case ICU       -> new IcuBedAllocator(bedRepository);
            case EMERGENCY -> new EmergencyBedAllocator(bedRepository);
            default        -> new GeneralBedAllocator(bedRepository);
        };
    }

    // ---- Concrete Allocator: ICU ----
    public static class IcuBedAllocator implements BedAllocator {
        private final BedRepository repo;
        public IcuBedAllocator(BedRepository repo) { this.repo = repo; }

        @Override
        public List<Bed> findSuitableBeds(Bed.WardType wardType) {
            // ICU: strict — only beds explicitly tagged ICU and AVAILABLE
            return repo.findByWardTypeAndStatus(Bed.WardType.ICU, Bed.BedStatus.AVAILABLE);
        }

        @Override
        public String getStrategyDescription() {
            return "ICU Strict Allocation — only ICU beds assigned to critical patients";
        }
    }

    // ---- Concrete Allocator: Emergency ----
    public static class EmergencyBedAllocator implements BedAllocator {
        private final BedRepository repo;
        public EmergencyBedAllocator(BedRepository repo) { this.repo = repo; }

        @Override
        public List<Bed> findSuitableBeds(Bed.WardType wardType) {
            // Emergency: priority — first emergency beds, then ICU if none available
            List<Bed> beds = repo.findByWardTypeAndStatus(Bed.WardType.EMERGENCY, Bed.BedStatus.AVAILABLE);
            if (beds.isEmpty()) {
                beds = repo.findByWardTypeAndStatus(Bed.WardType.ICU, Bed.BedStatus.AVAILABLE);
            }
            return beds;
        }

        @Override
        public String getStrategyDescription() {
            return "Emergency Priority Allocation — emergency beds first, ICU fallback";
        }
    }

    // ---- Concrete Allocator: General ----
    public static class GeneralBedAllocator implements BedAllocator {
        private final BedRepository repo;
        public GeneralBedAllocator(BedRepository repo) { this.repo = repo; }

        @Override
        public List<Bed> findSuitableBeds(Bed.WardType wardType) {
            // General: flexible — beds of the requested ward type
            return repo.findByWardTypeAndStatus(wardType, Bed.BedStatus.AVAILABLE);
        }

        @Override
        public String getStrategyDescription() {
            return "General Flexible Allocation — beds matched by requested ward type";
        }
    }
}
