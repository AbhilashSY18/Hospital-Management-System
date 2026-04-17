package com.hospital.repository;

import com.hospital.model.Admission;
import com.hospital.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdmissionRepository extends JpaRepository<Admission, Long> {

    List<Admission> findByStatus(Admission.AdmissionStatus status);

    Optional<Admission> findByPatientAndStatus(Patient patient, Admission.AdmissionStatus status);

    List<Admission> findByAdmissionDateBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(Admission.AdmissionStatus status);

    // FIX: Hibernate 6 cannot resolve fully-qualified Java enum paths in JPQL.
    // Use a named :status bind parameter instead — Spring Data passes the
    // enum value correctly via @Param.
    @Query("SELECT a FROM Admission a WHERE a.status = :status ORDER BY a.admissionDate DESC")
    List<Admission> findAllActiveAdmissions(@Param("status") Admission.AdmissionStatus status);
}
