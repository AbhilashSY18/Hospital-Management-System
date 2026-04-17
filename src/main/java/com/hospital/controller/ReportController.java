package com.hospital.controller;

import com.hospital.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public String reportsHome() {
        return "report/index";
    }

    @GetMapping("/beds")
    public String bedReport(Model model) {
        Map<String, Object> report = reportService.getBedUtilisationReport();
        model.addAllAttributes(report);
        return "report/beds";
    }

    @GetMapping("/resources")
    public String resourceReport(Model model) {
        Map<String, Object> report = reportService.getResourceUsageReport();
        model.addAllAttributes(report);
        return "report/resources";
    }

    @GetMapping("/admissions")
    public String admissionReport(Model model) {
        Map<String, Object> report = reportService.getAdmissionSummaryReport();
        model.addAllAttributes(report);
        return "report/admissions";
    }
}
