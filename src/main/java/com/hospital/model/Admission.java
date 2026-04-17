package com.hospital.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "admissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Admission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long admissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bed_id", nullable = false)
    private Bed bed;

    // FIX: admittedBy is OPTIONAL (null is passed from PatientController).
    // Must be optional=true so Hibernate does not enforce NOT NULL at insert time.
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "admitted_by", nullable = true)
    private User admittedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdmissionStatus status;

    private LocalDateTime admissionDate;
    private LocalDateTime dischargeDate;

    private String notes;

    public enum AdmissionStatus {
        REQUESTED, APPROVED, ACTIVE, COMPLETED
    }

    public boolean isActive() {
        return this.status == AdmissionStatus.ACTIVE;
    }
}
