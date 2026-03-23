package com.era.emergency.config;

import com.era.emergency.model.*;
import com.era.emergency.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the in-memory H2 database with sample hospitals and emergencies on startup.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final HospitalRepository hospitalRepository;
    private final EmergencyRequestRepository emergencyRepo;

    @Override
    public void run(String... args) {
        seedHospitals();
        seedEmergencies();
    }

    // ─── Hospitals ────────────────────────────────────────────────────────────

    private void seedHospitals() {
        List<Hospital> hospitals = List.of(
            Hospital.builder()
                .name("Apollo Hospitals")
                .address("Sarita Vihar, Delhi Mathura Road")
                .city("New Delhi")
                .latitude(28.5355).longitude(77.2810)
                .totalIcuBeds(60).availableIcuBeds(12)
                .totalGeneralBeds(400).availableGeneralBeds(80)
                .availableAmbulances(8)
                .specializations("Cardiology,Neurology,Trauma,Oncology")
                .isActive(true).traumaCenter(true)
                .build(),

            Hospital.builder()
                .name("AIIMS Delhi")
                .address("Sri Aurobindo Marg, Ansari Nagar")
                .city("New Delhi")
                .latitude(28.5673).longitude(77.2100)
                .totalIcuBeds(80).availableIcuBeds(5)
                .totalGeneralBeds(800).availableGeneralBeds(120)
                .availableAmbulances(12)
                .specializations("All Specialties,Trauma,Burn,Cardiology")
                .isActive(true).traumaCenter(true)
                .build(),

            Hospital.builder()
                .name("Fortis Escorts Heart Institute")
                .address("Okhla Road, Okhla")
                .city("New Delhi")
                .latitude(28.5601).longitude(77.2850)
                .totalIcuBeds(50).availableIcuBeds(18)
                .totalGeneralBeds(300).availableGeneralBeds(60)
                .availableAmbulances(6)
                .specializations("Cardiology,Cardiac Surgery,Pulmonology")
                .isActive(true).traumaCenter(false)
                .build(),

            Hospital.builder()
                .name("Max Super Speciality Hospital")
                .address("1, 2, Press Enclave Road, Saket")
                .city("New Delhi")
                .latitude(28.5244).longitude(77.2066)
                .totalIcuBeds(45).availableIcuBeds(22)
                .totalGeneralBeds(250).availableGeneralBeds(90)
                .availableAmbulances(5)
                .specializations("Neurology,Orthopedics,Oncology,Pediatrics")
                .isActive(true).traumaCenter(false)
                .build(),

            Hospital.builder()
                .name("Safdarjung Hospital")
                .address("Ring Road, Safdarjung")
                .city("New Delhi")
                .latitude(28.5692).longitude(77.2088)
                .totalIcuBeds(100).availableIcuBeds(30)
                .totalGeneralBeds(1500).availableGeneralBeds(300)
                .availableAmbulances(15)
                .specializations("General Surgery,Trauma,Burns,Obstetrics")
                .isActive(true).traumaCenter(true)
                .build(),

            Hospital.builder()
                .name("BLK-Max Super Speciality Hospital")
                .address("5, Pusa Road, Rajinder Nagar")
                .city("New Delhi")
                .latitude(28.6411).longitude(77.1708)
                .totalIcuBeds(40).availableIcuBeds(0)   // full – stress test
                .totalGeneralBeds(200).availableGeneralBeds(0)
                .availableAmbulances(0)
                .specializations("Transplant,Haematology,Oncology")
                .isActive(true).traumaCenter(false)
                .build(),

            Hospital.builder()
                .name("Sir Ganga Ram Hospital")
                .address("Rajinder Nagar, Old Rajinder Nagar")
                .city("New Delhi")
                .latitude(28.6450).longitude(77.1720)
                .totalIcuBeds(55).availableIcuBeds(20)
                .totalGeneralBeds(350).availableGeneralBeds(75)
                .availableAmbulances(7)
                .specializations("Gastroenterology,Nephrology,Urology,Pediatrics")
                .isActive(true).traumaCenter(false)
                .build()
        );

        hospitalRepository.saveAll(hospitals);
        log.info("✅ Seeded {} hospitals", hospitals.size());
    }

    // ─── Emergencies ──────────────────────────────────────────────────────────

    private void seedEmergencies() {
        List<EmergencyRequest> emergencies = List.of(
            EmergencyRequest.builder()
                .patientName("Rahul Sharma")
                .patientAge(45)
                .severity(Severity.CRITICAL)
                .emergencyType("Cardiac Arrest")
                .description("Patient collapsed, no pulse detected. CPR in progress.")
                .latitude(28.5450).longitude(77.2560)
                .locationAddress("Lajpat Nagar, New Delhi")
                .reporterContact("+91-9876543210")
                .status(EmergencyStatus.PENDING)
                .build(),

            EmergencyRequest.builder()
                .patientName("Priya Verma")
                .patientAge(28)
                .severity(Severity.HIGH)
                .emergencyType("Road Accident")
                .description("Multiple trauma injuries after vehicle collision. Bleeding from head.")
                .latitude(28.5800).longitude(77.2300)
                .locationAddress("ITO Crossing, New Delhi")
                .reporterContact("+91-9123456789")
                .status(EmergencyStatus.PENDING)
                .build(),

            EmergencyRequest.builder()
                .patientName("Mohan Das")
                .patientAge(62)
                .severity(Severity.MEDIUM)
                .emergencyType("Stroke")
                .description("Sudden facial drooping, slurred speech, right arm weakness.")
                .latitude(28.6100).longitude(77.2200)
                .locationAddress("Karol Bagh, New Delhi")
                .reporterContact("+91-9988776655")
                .status(EmergencyStatus.PENDING)
                .build(),

            EmergencyRequest.builder()
                .patientName("Anjali Singh")
                .patientAge(7)
                .severity(Severity.HIGH)
                .emergencyType("Severe Allergic Reaction")
                .description("Child with anaphylaxis after bee sting. Swelling of throat.")
                .latitude(28.5300).longitude(77.2150)
                .locationAddress("Saket, New Delhi")
                .reporterContact("+91-9871234560")
                .status(EmergencyStatus.PENDING)
                .build(),

            EmergencyRequest.builder()
                .patientName("Suresh Kumar")
                .patientAge(35)
                .severity(Severity.LOW)
                .emergencyType("Fracture")
                .description("Suspected fractured wrist from a fall. Patient stable.")
                .latitude(28.6500).longitude(77.1900)
                .locationAddress("Rajouri Garden, New Delhi")
                .reporterContact("+91-9001234567")
                .status(EmergencyStatus.PENDING)
                .build()
        );

        emergencyRepo.saveAll(emergencies);
        log.info("✅ Seeded {} emergency requests", emergencies.size());
    }
}
