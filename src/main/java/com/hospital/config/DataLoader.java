package com.hospital.config;

import com.hospital.model.*;
import com.hospital.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BedRepository bedRepository;
    private final PatientRepository patientRepository;
    private final MedicalResourceRepository resourceRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        loadUsers();
        loadBeds();
        loadPatients();
        loadResources();
        System.out.println("=== Sample data loaded. Login: admin/admin123, doctor/doctor123, nurse/nurse123 ===");
    }

    private void loadUsers() {
        if (userRepository.count() > 0) return;

        userRepository.save(User.builder().username("admin").password(passwordEncoder.encode("admin123"))
            .name("Admin User").email("admin@hospital.com").role(User.Role.ADMIN).enabled(true).build());
        userRepository.save(User.builder().username("doctor").password(passwordEncoder.encode("doctor123"))
            .name("Dr. Rajesh Kumar").email("rajesh@hospital.com").role(User.Role.DOCTOR).enabled(true).build());
        userRepository.save(User.builder().username("nurse").password(passwordEncoder.encode("nurse123"))
            .name("Nurse Priya").email("priya@hospital.com").role(User.Role.NURSE).enabled(true).build());
        userRepository.save(User.builder().username("reception").password(passwordEncoder.encode("reception123"))
            .name("Receptionist Anita").email("anita@hospital.com").role(User.Role.RECEPTIONIST).enabled(true).build());
    }

    private void loadBeds() {
        if (bedRepository.count() > 0) return;

        // ICU Beds
        for (int i = 1; i <= 5; i++) {
            bedRepository.save(Bed.builder().bedNumber("ICU-" + String.format("%02d", i))
                .wardType(Bed.WardType.ICU).status(Bed.BedStatus.AVAILABLE).location("Floor 1, ICU Wing").build());
        }
        // General Beds
        for (int i = 1; i <= 10; i++) {
            bedRepository.save(Bed.builder().bedNumber("GEN-" + String.format("%02d", i))
                .wardType(Bed.WardType.GENERAL).status(Bed.BedStatus.AVAILABLE).location("Floor 2, General Ward").build());
        }
        // Emergency Beds
        for (int i = 1; i <= 4; i++) {
            bedRepository.save(Bed.builder().bedNumber("EMR-" + String.format("%02d", i))
                .wardType(Bed.WardType.EMERGENCY).status(Bed.BedStatus.AVAILABLE).location("Ground Floor, ER").build());
        }
        // Mark some as occupied for demo
        bedRepository.findAll().stream().filter(b -> b.getBedNumber().equals("GEN-01"))
            .findFirst().ifPresent(b -> { b.setStatus(Bed.BedStatus.OCCUPIED); bedRepository.save(b); });
        bedRepository.findAll().stream().filter(b -> b.getBedNumber().equals("ICU-01"))
            .findFirst().ifPresent(b -> { b.setStatus(Bed.BedStatus.OCCUPIED); bedRepository.save(b); });
    }

    private void loadPatients() {
        if (patientRepository.count() > 0) return;

        patientRepository.save(Patient.builder().name("Arjun Sharma").age(45).gender(Patient.Gender.MALE)
            .contactNumber("9876543210").condition("Cardiac Arrest").admissionStatus(Patient.AdmissionStatus.ADMITTED)
            .registrationDate(LocalDate.now().minusDays(2)).build());
        patientRepository.save(Patient.builder().name("Kavitha Rao").age(32).gender(Patient.Gender.FEMALE)
            .contactNumber("9123456789").condition("Appendicitis").admissionStatus(Patient.AdmissionStatus.ADMITTED)
            .registrationDate(LocalDate.now().minusDays(1)).build());
        patientRepository.save(Patient.builder().name("Suresh Nair").age(60).gender(Patient.Gender.MALE)
            .contactNumber("9988776655").condition("Diabetes Complication").admissionStatus(Patient.AdmissionStatus.REGISTERED)
            .registrationDate(LocalDate.now()).build());
        patientRepository.save(Patient.builder().name("Meena Pillai").age(28).gender(Patient.Gender.FEMALE)
            .contactNumber("9765432101").condition("Fracture - Left Leg").admissionStatus(Patient.AdmissionStatus.DISCHARGED)
            .registrationDate(LocalDate.now().minusDays(5)).build());
    }

    private void loadResources() {
        if (resourceRepository.count() > 0) return;

        resourceRepository.save(MedicalResource.builder().resourceName("Ventilator A")
            .type(MedicalResource.ResourceType.VENTILATOR).availabilityStatus(MedicalResource.AvailabilityStatus.AVAILABLE)
            .totalQuantity(1).availableQuantity(1).location("ICU Wing").build());
        resourceRepository.save(MedicalResource.builder().resourceName("Ventilator B")
            .type(MedicalResource.ResourceType.VENTILATOR).availabilityStatus(MedicalResource.AvailabilityStatus.ALLOCATED)
            .totalQuantity(1).availableQuantity(0).location("ICU Wing").build());
        resourceRepository.save(MedicalResource.builder().resourceName("Oxygen Cylinder Bank 1")
            .type(MedicalResource.ResourceType.OXYGEN_CYLINDER).availabilityStatus(MedicalResource.AvailabilityStatus.AVAILABLE)
            .totalQuantity(10).availableQuantity(7).location("Store Room").build());
        resourceRepository.save(MedicalResource.builder().resourceName("Patient Monitor 1")
            .type(MedicalResource.ResourceType.MONITOR).availabilityStatus(MedicalResource.AvailabilityStatus.AVAILABLE)
            .totalQuantity(1).availableQuantity(1).location("General Ward").build());
        resourceRepository.save(MedicalResource.builder().resourceName("Patient Monitor 2")
            .type(MedicalResource.ResourceType.MONITOR).availabilityStatus(MedicalResource.AvailabilityStatus.AVAILABLE)
            .totalQuantity(1).availableQuantity(1).location("ICU Wing").build());
        resourceRepository.save(MedicalResource.builder().resourceName("Defibrillator")
            .type(MedicalResource.ResourceType.DEFIBRILLATOR).availabilityStatus(MedicalResource.AvailabilityStatus.AVAILABLE)
            .totalQuantity(1).availableQuantity(1).location("Emergency").build());
    }
}
