package com.hospital.repository;

import com.hospital.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient> findByAdmissionStatus(Patient.AdmissionStatus status);
    List<Patient> findByNameContainingIgnoreCase(String name);
}
