package com.hospital.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "beds")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bedId;

    @Column(nullable = false, unique = true)
    private String bedNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WardType wardType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BedStatus status;

    private String location; // e.g. "Floor 2, Room 201"

    public enum WardType {
        ICU, GENERAL, EMERGENCY, PEDIATRIC, MATERNITY
    }

    public enum BedStatus {
        AVAILABLE, OCCUPIED, CLEANING, MAINTENANCE
    }

    public boolean isAvailable() {
        return this.status == BedStatus.AVAILABLE;
    }
}
