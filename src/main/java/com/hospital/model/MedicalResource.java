package com.hospital.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medical_resources")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resourceId;

    @Column(nullable = false)
    private String resourceName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResourceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus availabilityStatus;

    private int totalQuantity;
    private int availableQuantity;

    private String location;

    // Patient currently using this resource (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_patient_id")
    private Patient assignedPatient;

    public enum ResourceType {
        VENTILATOR, OXYGEN_CYLINDER, MONITOR, WHEELCHAIR, DEFIBRILLATOR
    }

    public enum AvailabilityStatus {
        AVAILABLE, ALLOCATED, IN_USE, MAINTENANCE
    }

    public boolean isAvailable() {
        return this.availableQuantity > 0
            && this.availabilityStatus == AvailabilityStatus.AVAILABLE;
    }
}
