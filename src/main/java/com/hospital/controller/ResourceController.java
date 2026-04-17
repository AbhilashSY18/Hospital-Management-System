package com.hospital.controller;

import com.hospital.model.MedicalResource;
import com.hospital.service.PatientService;
import com.hospital.service.ResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;
    private final PatientService patientService;

    @GetMapping
    public String listResources(Model model) {
        model.addAttribute("resources", resourceService.getAllResources());
        model.addAttribute("newResource", new MedicalResource());
        model.addAttribute("resourceTypes", MedicalResource.ResourceType.values());
        model.addAttribute("statuses", MedicalResource.AvailabilityStatus.values());
        return "resource/list";
    }

    @PostMapping("/add")
    public String addResource(@ModelAttribute MedicalResource resource, RedirectAttributes ra) {
        resource.setAvailabilityStatus(MedicalResource.AvailabilityStatus.AVAILABLE);
        resource.setAvailableQuantity(resource.getTotalQuantity());
        resourceService.saveResource(resource);
        ra.addFlashAttribute("success", "Resource added: " + resource.getResourceName());
        return "redirect:/resources";
    }

    @GetMapping("/allocate")
    public String allocateForm(Model model) {
        model.addAttribute("availableResources", resourceService.getAvailableResources());
        model.addAttribute("admittedPatients",
            patientService.getPatientsByStatus(com.hospital.model.Patient.AdmissionStatus.ADMITTED));
        return "resource/allocate";
    }

    @PostMapping("/allocate")
    public String allocateResource(@RequestParam Long resourceId,
                                    @RequestParam Long patientId,
                                    RedirectAttributes ra) {
        boolean success = resourceService.allocateResource(resourceId, patientId);
        if (success) {
            ra.addFlashAttribute("success", "Resource allocated successfully.");
        } else {
            ra.addFlashAttribute("error", "Allocation failed: resource unavailable or already assigned.");
        }
        return "redirect:/resources";
    }

    @PostMapping("/release/{resourceId}")
    public String releaseResource(@PathVariable Long resourceId, RedirectAttributes ra) {
        boolean success = resourceService.releaseResource(resourceId);
        if (success) {
            ra.addFlashAttribute("success", "Resource released successfully.");
        } else {
            ra.addFlashAttribute("error", "Release failed.");
        }
        return "redirect:/resources";
    }

    @PostMapping("/delete/{id}")
    public String deleteResource(@PathVariable Long id, RedirectAttributes ra) {
        resourceService.deleteResource(id);
        ra.addFlashAttribute("success", "Resource deleted.");
        return "redirect:/resources";
    }
}
