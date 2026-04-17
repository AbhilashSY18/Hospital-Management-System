package com.hospital.repository;

import com.hospital.model.Bed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {

    List<Bed> findByStatus(Bed.BedStatus status);

    List<Bed> findByWardType(Bed.WardType wardType);

    List<Bed> findByWardTypeAndStatus(Bed.WardType wardType, Bed.BedStatus status);

    long countByStatus(Bed.BedStatus status);

    long countByWardType(Bed.WardType wardType);

    @Query("SELECT b.wardType, COUNT(b) FROM Bed b WHERE b.status = 'AVAILABLE' GROUP BY b.wardType")
    List<Object[]> countAvailableByWardType();

    @Query("SELECT b.wardType, COUNT(b) FROM Bed b GROUP BY b.wardType")
    List<Object[]> countTotalByWardType();
}
