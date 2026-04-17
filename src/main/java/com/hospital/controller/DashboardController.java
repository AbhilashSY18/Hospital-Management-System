package com.hospital.controller;

import com.hospital.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final BedService bedService;
    private final AdmissionService admissionService;
    private final PatientService patientService;
    private final ResourceService resourceService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalBeds", bedService.countTotal());
        model.addAttribute("availableBeds", bedService.countAvailable());
        model.addAttribute("occupiedBeds", bedService.countOccupied());
        model.addAttribute("activeAdmissions", admissionService.countActive());
        model.addAttribute("totalPatients", patientService.countTotal());
        model.addAttribute("availableResources", resourceService.countAvailable());
        model.addAttribute("totalResources", resourceService.countTotal());
        model.addAttribute("recentAdmissions", admissionService.getActiveAdmissions());
        return "dashboard";
    }
}
