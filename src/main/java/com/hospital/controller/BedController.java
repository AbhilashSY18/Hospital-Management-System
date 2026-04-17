package com.hospital.controller;

import com.hospital.model.Bed;
import com.hospital.service.BedService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/beds")
@RequiredArgsConstructor
public class BedController {

    private final BedService bedService;

    @GetMapping
    public String listBeds(Model model) {
        model.addAttribute("beds", bedService.getAllBeds());
        model.addAttribute("wardTypes", Bed.WardType.values());
        model.addAttribute("statuses", Bed.BedStatus.values());
        model.addAttribute("newBed", new Bed());
        return "bed/list";
    }

    // FIX: /beds/available now reuses bed/list template with filtered data
    // instead of returning non-existent "bed/available" template.
    @GetMapping("/available")
    public String availableBeds(@RequestParam(required = false) Bed.WardType wardType, Model model) {
        if (wardType != null) {
            model.addAttribute("beds", bedService.getAvailableBedsByWard(wardType));
            model.addAttribute("selectedWard", wardType);
        } else {
            model.addAttribute("beds", bedService.getAvailableBeds());
        }
        model.addAttribute("wardTypes", Bed.WardType.values());
        model.addAttribute("statuses", Bed.BedStatus.values());
        model.addAttribute("newBed", new Bed());
        return "bed/list"; // FIX: was "bed/available" which does not exist
    }

    @PostMapping("/add")
    public String addBed(@ModelAttribute Bed bed, RedirectAttributes ra) {
        bed.setStatus(Bed.BedStatus.AVAILABLE);
        bedService.saveBed(bed);
        ra.addFlashAttribute("success", "Bed " + bed.getBedNumber() + " added successfully.");
        return "redirect:/beds";
    }

    @PostMapping("/status/{bedId}")
    public String updateStatus(@PathVariable Long bedId,
                                @RequestParam Bed.BedStatus status,
                                RedirectAttributes ra) {
        try {
            Bed updated = bedService.updateBedStatus(bedId, status);
            ra.addFlashAttribute("success", "Bed " + updated.getBedNumber() + " status updated to " + status);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/beds";
    }

    @PostMapping("/delete/{bedId}")
    public String deleteBed(@PathVariable Long bedId, RedirectAttributes ra) {
        bedService.deleteBed(bedId);
        ra.addFlashAttribute("success", "Bed deleted.");
        return "redirect:/beds";
    }
}
