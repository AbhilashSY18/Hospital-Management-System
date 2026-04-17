package com.hospital.pattern;

import com.hospital.model.MedicalResource;
import com.hospital.model.Patient;

/**
 * DESIGN PATTERN: Decorator (Structural)
 *
 * Intent: Dynamically adds responsibilities to an object without modifying it.
 * Uses composition over inheritance to extend behaviour at runtime.
 *
 * Application to domain: The base ResourceAllocator handles allocation.
 * The LoggingResourceAllocator decorator wraps it and adds audit logging.
 * The ValidationResourceAllocator decorator adds overallocation prevention.
 * These can be stacked: Validation wraps Logging wraps Base.
 */
public class ResourceAllocatorDecorator {

    // ---- Component interface ----
    public interface ResourceAllocator {
        boolean allocate(MedicalResource resource, Patient patient);
        boolean release(MedicalResource resource);
        String getDescription();
    }

    // ---- Concrete Component: Base Allocator ----
    public static class BaseResourceAllocator implements ResourceAllocator {

        @Override
        public boolean allocate(MedicalResource resource, Patient patient) {
            if (resource.getAvailableQuantity() <= 0) return false;
            resource.setAvailableQuantity(resource.getAvailableQuantity() - 1);
            resource.setAssignedPatient(patient);
            if (resource.getAvailableQuantity() == 0) {
                resource.setAvailabilityStatus(MedicalResource.AvailabilityStatus.ALLOCATED);
            }
            return true;
        }

        @Override
        public boolean release(MedicalResource resource) {
            resource.setAvailableQuantity(resource.getAvailableQuantity() + 1);
            resource.setAssignedPatient(null);
            resource.setAvailabilityStatus(MedicalResource.AvailabilityStatus.AVAILABLE);
            return true;
        }

        @Override
        public String getDescription() {
            return "Base Resource Allocator";
        }
    }

    // ---- Abstract Decorator ----
    public abstract static class ResourceAllocatorDecoratorBase implements ResourceAllocator {
        protected final ResourceAllocator wrapped;

        public ResourceAllocatorDecoratorBase(ResourceAllocator wrapped) {
            this.wrapped = wrapped;
        }
    }

    // ---- Concrete Decorator 1: Logging ----
    public static class LoggingResourceAllocator extends ResourceAllocatorDecoratorBase {

        public LoggingResourceAllocator(ResourceAllocator wrapped) {
            super(wrapped);
        }

        @Override
        public boolean allocate(MedicalResource resource, Patient patient) {
            System.out.printf("[AUDIT] Allocating %s (ID=%d) to patient %s (ID=%d)%n",
                resource.getResourceName(), resource.getResourceId(),
                patient.getName(), patient.getPatientId());
            boolean result = wrapped.allocate(resource, patient);
            System.out.printf("[AUDIT] Allocation %s for %s%n",
                result ? "SUCCESSFUL" : "FAILED", resource.getResourceName());
            return result;
        }

        @Override
        public boolean release(MedicalResource resource) {
            System.out.printf("[AUDIT] Releasing %s (ID=%d)%n",
                resource.getResourceName(), resource.getResourceId());
            return wrapped.release(resource);
        }

        @Override
        public String getDescription() {
            return wrapped.getDescription() + " + Logging";
        }
    }

    // ---- Concrete Decorator 2: Overallocation Validation ----
    public static class ValidationResourceAllocator extends ResourceAllocatorDecoratorBase {

        public ValidationResourceAllocator(ResourceAllocator wrapped) {
            super(wrapped);
        }

        @Override
        public boolean allocate(MedicalResource resource, Patient patient) {
            // Prevent overallocation: check available quantity first
            if (resource.getAvailableQuantity() <= 0) {
                System.out.printf("[VALIDATION] Blocked allocation: %s has no available units%n",
                    resource.getResourceName());
                return false;
            }
            // Prevent duplicate assignment to same patient
            if (patient.equals(resource.getAssignedPatient())) {
                System.out.printf("[VALIDATION] Blocked: %s already assigned to patient %s%n",
                    resource.getResourceName(), patient.getName());
                return false;
            }
            return wrapped.allocate(resource, patient);
        }

        @Override
        public boolean release(MedicalResource resource) {
            if (resource.getAvailableQuantity() >= resource.getTotalQuantity()) {
                System.out.println("[VALIDATION] Release skipped: resource already fully available");
                return false;
            }
            return wrapped.release(resource);
        }

        @Override
        public String getDescription() {
            return wrapped.getDescription() + " + Validation";
        }
    }

    /**
     * Factory helper to build the fully-decorated allocator.
     * Stacks: Validation -> Logging -> Base
     */
    public static ResourceAllocator createFullyDecoratedAllocator() {
        ResourceAllocator base = new BaseResourceAllocator();
        ResourceAllocator withLogging = new LoggingResourceAllocator(base);
        return new ValidationResourceAllocator(withLogging);
    }
}
