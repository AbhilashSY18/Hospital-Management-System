package com.hospital.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long patientId;

    @Column(nullable = false)
    private String name;

    private int age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String contactNumber;

    private String address;

    @Column(nullable = false)
    private String condition; // medical condition/diagnosis

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdmissionStatus admissionStatus;

    private LocalDate registrationDate;

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    public enum AdmissionStatus {
        REGISTERED, ADMITTED, DISCHARGED
    }
}
