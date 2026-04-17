package com.hospital.pattern;

import com.hospital.repository.AdmissionRepository;
import com.hospital.repository.BedRepository;
import com.hospital.repository.MedicalResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * DESIGN PATTERN: Template Method (Behavioral) — 4th pattern
 *
 * Intent: Defines the skeleton of a report-generation algorithm in a base class,
 * deferring specific steps to subclasses. Each report type overrides only
 * the steps that differ.
 *
 * Application to domain: All reports follow the same steps:
 *   1. validateParameters()
 *   2. fetchData()
 *   3. formatReport()
 *   4. appendSummary()
 * But each report type (Bed Utilisation, Resource Usage, Admission Summary)
 * fetches and formats data differently. The template method enforces the sequence.
 *
 * NOTE: Spring's @Service annotation also enforces the Singleton pattern
 * (one shared instance per application context) — this is the framework-provided pattern.
 */
public class ReportTemplateMethod {

    // ---- Abstract base report (Template) ----
    public abstract static class ReportGenerator {
        protected final BedRepository bedRepository;
        protected final AdmissionRepository admissionRepository;
        protected final MedicalResourceRepository resourceRepository;

        public ReportGenerator(BedRepository b, AdmissionRepository a,
                                MedicalResourceRepository r) {
            this.bedRepository = b;
            this.admissionRepository = a;
            this.resourceRepository = r;
        }

        // Template method — defines the algorithm skeleton
        public final Map<String, Object> generateReport() {
            validateParameters();
            Map<String, Object> data = fetchData();
            Map<String, Object> formatted = formatReport(data);
            appendSummary(formatted);
            return formatted;
        }

        protected void validateParameters() {
            // Default: no-op (subclasses may override)
        }

        protected abstract Map<String, Object> fetchData();

        protected abstract Map<String, Object> formatReport(Map<String, Object> rawData);

        protected void appendSummary(Map<String, Object> report) {
            report.put("generatedAt", java.time.LocalDateTime.now().toString());
        }
    }

    // ---- Concrete Report 1: Bed Utilisation ----
    @Component
    public static class BedUtilisationReport extends ReportGenerator {

        @Autowired
        public BedUtilisationReport(BedRepository b, AdmissionRepository a,
                                     MedicalResourceRepository r) {
            super(b, a, r);
        }

        @Override
        protected Map<String, Object> fetchData() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("totalBeds", bedRepository.count());
            data.put("availableBeds", bedRepository.countByStatus(
                com.hospital.model.Bed.BedStatus.AVAILABLE));
            data.put("occupiedBeds", bedRepository.countByStatus(
                com.hospital.model.Bed.BedStatus.OCCUPIED));
            data.put("maintenanceBeds", bedRepository.countByStatus(
                com.hospital.model.Bed.BedStatus.MAINTENANCE));
            data.put("availableByWard", bedRepository.countAvailableByWardType());
            data.put("totalByWard", bedRepository.countTotalByWardType());
            return data;
        }

        @Override
        protected Map<String, Object> formatReport(Map<String, Object> rawData) {
            long total = (long) rawData.get("totalBeds");
            long occupied = (long) rawData.get("occupiedBeds");
            double occupancyRate = total > 0 ? (double) occupied / total * 100 : 0;
            rawData.put("occupancyRate", String.format("%.1f%%", occupancyRate));
            rawData.put("reportType", "Bed Utilisation Report");
            return rawData;
        }
    }

    // ---- Concrete Report 2: Resource Usage ----
    @Component
    public static class ResourceUsageReport extends ReportGenerator {

        @Autowired
        public ResourceUsageReport(BedRepository b, AdmissionRepository a,
                                    MedicalResourceRepository r) {
            super(b, a, r);
        }

        @Override
        protected Map<String, Object> fetchData() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("totalResources", resourceRepository.count());
            data.put("availableResources", resourceRepository.countByAvailabilityStatus(
                com.hospital.model.MedicalResource.AvailabilityStatus.AVAILABLE));
            data.put("allocatedResources", resourceRepository.countByAvailabilityStatus(
                com.hospital.model.MedicalResource.AvailabilityStatus.ALLOCATED));
            data.put("allResources", resourceRepository.findAll());
            return data;
        }

        @Override
        protected Map<String, Object> formatReport(Map<String, Object> rawData) {
            long total = (long) rawData.get("totalResources");
            long allocated = (long) rawData.get("allocatedResources");
            double utilRate = total > 0 ? (double) allocated / total * 100 : 0;
            rawData.put("utilisationRate", String.format("%.1f%%", utilRate));
            rawData.put("reportType", "Resource Usage Report");
            return rawData;
        }
    }

    // ---- Concrete Report 3: Admission Summary ----
    @Component
    public static class AdmissionSummaryReport extends ReportGenerator {

        @Autowired
        public AdmissionSummaryReport(BedRepository b, AdmissionRepository a,
                                       MedicalResourceRepository r) {
            super(b, a, r);
        }

        @Override
        protected Map<String, Object> fetchData() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("totalAdmissions", admissionRepository.count());
            data.put("activeAdmissions", admissionRepository.countByStatus(
                com.hospital.model.Admission.AdmissionStatus.ACTIVE));
            data.put("completedAdmissions", admissionRepository.countByStatus(
                com.hospital.model.Admission.AdmissionStatus.COMPLETED));
            data.put("activeList", admissionRepository.findAllActiveAdmissions(com.hospital.model.Admission.AdmissionStatus.ACTIVE));
            return data;
        }

        @Override
        protected Map<String, Object> formatReport(Map<String, Object> rawData) {
            rawData.put("reportType", "Daily Admission Summary");
            return rawData;
        }
    }
}
