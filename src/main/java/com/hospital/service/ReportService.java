package com.hospital.service;

import com.hospital.pattern.ReportTemplateMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Delegates to the appropriate Template Method report generator.
 * Spring manages each generator as a singleton @Component.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportTemplateMethod.BedUtilisationReport bedUtilisationReport;
    private final ReportTemplateMethod.ResourceUsageReport resourceUsageReport;
    private final ReportTemplateMethod.AdmissionSummaryReport admissionSummaryReport;

    public Map<String, Object> getBedUtilisationReport() {
        return bedUtilisationReport.generateReport();
    }

    public Map<String, Object> getResourceUsageReport() {
        return resourceUsageReport.generateReport();
    }

    public Map<String, Object> getAdmissionSummaryReport() {
        return admissionSummaryReport.generateReport();
    }
}
