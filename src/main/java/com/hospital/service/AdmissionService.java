package com.hospital.service;

import com.hospital.model.*;
import com.hospital.pattern.BedStatusObserver;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * DESIGN PRINCIPLE: Dependency Inversion Principle (DIP)
 * AdmissionService depends on repository abstractions (interfaces),
 * not concrete implementations. Spring injects the correct implementations.
 *
 * DESIGN PRINCIPLE: Interface Segregation Principle (ISP)
 * Each repository exposes only the methods its clients need.
 */
@Service
@RequiredArgsConstructor
public class AdmissionService {

    private final AdmissionRepository admissionRepository;
    private final PatientRepository patientRepository;
    private final BedRepository bedRepository;
    private final UserRepository userRepository;
    private final BedStatusObserver.BedStatusEventPublisher eventPublisher;

    public List<Admission> getAllAdmissions() {
        return admissionRepository.findAll();
    }

    public List<Admission> getActiveAdmissions() {
        return admissionRepository.findAllActiveAdmissions(Admission.AdmissionStatus.ACTIVE);
    }

    public Optional<Admission> getAdmissionById(Long id) {
        return admissionRepository.findById(id);
    }

    /**
     * Admit a patient: assign a bed and create an Admission record.
     * Verifies bed availability before proceeding.
     */
    @Transactional
    public Admission admitPatient(Long patientId, Long bedId, Long doctorUserId, String notes) {
        Patient patient = patientRepository.findById(patientId)
            .orElseThrow(() -> new RuntimeException("Patient not found: " + patientId));

        Bed bed = bedRepository.findById(bedId)
            .orElseThrow(() -> new RuntimeException("Bed not found: " + bedId));

        if (!bed.isAvailable()) {
            throw new RuntimeException("Bed " + bed.getBedNumber() + " is not available.");
        }

        // Mark bed as occupied — Observer will fire
        Bed.BedStatus prev = bed.getStatus();
        bed.setStatus(Bed.BedStatus.OCCUPIED);
        bedRepository.save(bed);
        eventPublisher.publishStatusChange(this, bed, prev, Bed.BedStatus.OCCUPIED);

        // Update patient status
        patient.setAdmissionStatus(Patient.AdmissionStatus.ADMITTED);
        patientRepository.save(patient);

        // Resolve the admitting user (doctor) — null is accepted when called without auth context
        User admittedBy = null;
        if (doctorUserId != null) {
            admittedBy = userRepository.findById(doctorUserId).orElse(null);
        }

        // Create admission record
        Admission admission = Admission.builder()
            .patient(patient)
            .bed(bed)
            .admittedBy(admittedBy)
            .status(Admission.AdmissionStatus.ACTIVE)
            .admissionDate(LocalDateTime.now())
            .notes(notes)
            .build();

        return admissionRepository.save(admission);
    }

    /**
     * Discharge a patient: release the bed and mark admission as completed.
     */
    @Transactional
    public Admission dischargePatient(Long admissionId) {
        Admission admission = admissionRepository.findById(admissionId)
            .orElseThrow(() -> new RuntimeException("Admission not found: " + admissionId));

        if (!admission.isActive()) {
            throw new RuntimeException("Admission is not currently active.");
        }

        // Set bed to CLEANING (Observer notifies resource release listener)
        Bed bed = admission.getBed();
        Bed.BedStatus prev = bed.getStatus();
        bed.setStatus(Bed.BedStatus.CLEANING);
        bedRepository.save(bed);
        eventPublisher.publishStatusChange(this, bed, prev, Bed.BedStatus.CLEANING);

        // Update patient status
        Patient patient = admission.getPatient();
        patient.setAdmissionStatus(Patient.AdmissionStatus.DISCHARGED);
        patientRepository.save(patient);

        // Close admission
        admission.setStatus(Admission.AdmissionStatus.COMPLETED);
        admission.setDischargeDate(LocalDateTime.now());
        return admissionRepository.save(admission);
    }

    /**
     * Transfer patient: release old bed, assign new bed.
     */
    @Transactional
    public Admission transferPatient(Long admissionId, Long newBedId) {
        Admission admission = admissionRepository.findById(admissionId)
            .orElseThrow(() -> new RuntimeException("Admission not found: " + admissionId));

        Bed newBed = bedRepository.findById(newBedId)
            .orElseThrow(() -> new RuntimeException("Bed not found: " + newBedId));

        if (!newBed.isAvailable()) {
            throw new RuntimeException("Target bed " + newBed.getBedNumber() + " is not available.");
        }

        // Release old bed
        Bed oldBed = admission.getBed();
        Bed.BedStatus oldPrev = oldBed.getStatus();
        oldBed.setStatus(Bed.BedStatus.CLEANING);
        bedRepository.save(oldBed);
        eventPublisher.publishStatusChange(this, oldBed, oldPrev, Bed.BedStatus.CLEANING);

        // Assign new bed
        Bed.BedStatus newPrev = newBed.getStatus();
        newBed.setStatus(Bed.BedStatus.OCCUPIED);
        bedRepository.save(newBed);
        eventPublisher.publishStatusChange(this, newBed, newPrev, Bed.BedStatus.OCCUPIED);

        admission.setBed(newBed);
        return admissionRepository.save(admission);
    }

    public long countActive() {
        return admissionRepository.countByStatus(Admission.AdmissionStatus.ACTIVE);
    }
}
