package com.hospital.pattern;

import com.hospital.model.Bed;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * DESIGN PATTERN: Observer (Behavioral) — using Spring's event system
 *
 * Intent: Defines a one-to-many dependency between objects.
 * When the subject (Bed) changes state, all registered observers are notified
 * automatically without tight coupling.
 *
 * Application to domain: When a bed's status changes (e.g., from OCCUPIED to
 * CLEANING after discharge), multiple parts of the system need to react:
 * - The dashboard must refresh availability counts
 * - The resource manager must free associated resources
 * - An audit log entry must be created
 * Each concern is handled by a separate Observer, keeping them decoupled.
 */
public class BedStatusObserver {

    // ---- Event (Subject notification) ----
    public static class BedStatusChangedEvent extends ApplicationEvent {
        private final Bed bed;
        private final Bed.BedStatus previousStatus;
        private final Bed.BedStatus newStatus;

        public BedStatusChangedEvent(Object source, Bed bed,
                                      Bed.BedStatus previousStatus,
                                      Bed.BedStatus newStatus) {
            super(source);
            this.bed = bed;
            this.previousStatus = previousStatus;
            this.newStatus = newStatus;
        }

        public Bed getBed() { return bed; }
        public Bed.BedStatus getPreviousStatus() { return previousStatus; }
        public Bed.BedStatus getNewStatus() { return newStatus; }
    }

    // ---- Observer 1: Availability Dashboard Listener ----
    @Component
    public static class DashboardRefreshListener {
        @EventListener
        public void onBedStatusChanged(BedStatusChangedEvent event) {
            System.out.printf("[DASHBOARD] Bed %s changed: %s → %s. Refreshing availability counts.%n",
                event.getBed().getBedNumber(),
                event.getPreviousStatus(),
                event.getNewStatus());
        }
    }

    // ---- Observer 2: Audit Log Listener ----
    @Component
    public static class AuditLogListener {
        @EventListener
        public void onBedStatusChanged(BedStatusChangedEvent event) {
            System.out.printf("[AUDIT LOG] %s | Bed[%s] Ward[%s] | %s → %s%n",
                java.time.LocalDateTime.now(),
                event.getBed().getBedNumber(),
                event.getBed().getWardType(),
                event.getPreviousStatus(),
                event.getNewStatus());
        }
    }

    // ---- Observer 3: Resource Release Listener ----
    @Component
    public static class ResourceReleaseListener {
        @EventListener
        public void onBedStatusChanged(BedStatusChangedEvent event) {
            // When a bed moves to CLEANING (post-discharge), trigger resource release
            if (event.getNewStatus() == Bed.BedStatus.CLEANING) {
                System.out.printf("[RESOURCES] Bed %s discharged — triggering resource cleanup.%n",
                    event.getBed().getBedNumber());
            }
        }
    }

    // ---- Publisher helper (used by BedService) ----
    @Component
    public static class BedStatusEventPublisher {
        private final ApplicationEventPublisher publisher;

        public BedStatusEventPublisher(ApplicationEventPublisher publisher) {
            this.publisher = publisher;
        }

        public void publishStatusChange(Object source, Bed bed,
                                         Bed.BedStatus prev, Bed.BedStatus next) {
            publisher.publishEvent(new BedStatusChangedEvent(source, bed, prev, next));
        }
    }
}
