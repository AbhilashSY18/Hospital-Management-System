package com.hospital;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Smart Hospital Bed and Resource Allocation System
 * OOAD Mini Project - UE23CS352B
 * Team 02, SEC A
 *
 * Team Members:
 *   - Aditya D Rao       (PES2UG23CS031) — Bed Management & Allocation
 *   - Arvind Jayanth BS  (PES2UG24CS806) — Patient Admission & Discharge
 *   - Manjunath K Byadagi (PES2UG24CS813) — Resource Allocation
 *   - Abhilash S Y       (PES2UG23CS018) — Reports & Admin
 */
@SpringBootApplication
public class HospitalApplication {
    public static void main(String[] args) {
        SpringApplication.run(HospitalApplication.class, args);
    }
}
