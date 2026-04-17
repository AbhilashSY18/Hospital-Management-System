package com.hospital.controller;

import com.hospital.model.*;
import com.hospital.repository.UserRepository;
import com.hospital.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;
    private final AdmissionService admissionService;
    private final BedService bedService;
    private final UserRepository userRepository;

    @GetMapping
    public String listPatients(Model model) {
        model.addAttribute("patients", patientService.getAllPatients());
        model.addAttribute("newPatient", new Patient());
        model.addAttribute("genders", Patient.Gender.values());
        return "patient/list";
    }

    @PostMapping("/register")
    public String registerPatient(@ModelAttribute Patient patient, RedirectAttributes ra) {
        patientService.registerPatient(patient);
        ra.addFlashAttribute("success", "Patient " + patient.getName() + " registered successfully.");
        return "redirect:/patients";
    }

    @GetMapping("/admit")
    public String admitForm(Model model) {
        model.addAttribute("registeredPatients",
            patientService.getPatientsByStatus(Patient.AdmissionStatus.REGISTERED));
        model.addAttribute("availableBeds", bedService.getAvailableBeds());
        model.addAttribute("wardTypes", Bed.WardType.values());
        return "patient/admit";
    }

    @PostMapping("/admit")
    public String admitPatient(@RequestParam Long patientId,
                                @RequestParam Long bedId,
                                @RequestParam(required = false) String notes,
                                Principal principal,
                                RedirectAttributes ra) {
        try {
            Long doctorId = null;
            if (principal != null) {
                doctorId = userRepository.findByUsername(principal.getName())
                    .map(u -> u.getUserId()).orElse(null);
            }
            Admission admission = admissionService.admitPatient(patientId, bedId, doctorId, notes);
            ra.addFlashAttribute("success",
                "Patient admitted. Admission ID: " + admission.getAdmissionId());
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/patients";
    }

    @PostMapping("/discharge/{admissionId}")
    public String dischargePatient(@PathVariable Long admissionId, RedirectAttributes ra) {
        try {
            admissionService.dischargePatient(admissionId);
            ra.addFlashAttribute("success", "Patient discharged successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/patients/admissions";
    }

    @GetMapping("/admissions")
    public String listAdmissions(Model model) {
        model.addAttribute("admissions", admissionService.getActiveAdmissions());
        model.addAttribute("availableBeds", bedService.getAvailableBeds());
        return "patient/admissions";
    }

    @PostMapping("/transfer/{admissionId}")
    public String transferPatient(@PathVariable Long admissionId,
                                   @RequestParam Long newBedId,
                                   RedirectAttributes ra) {
        try {
            admissionService.transferPatient(admissionId, newBedId);
            ra.addFlashAttribute("success", "Patient transferred successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/patients/admissions";
    }
}
