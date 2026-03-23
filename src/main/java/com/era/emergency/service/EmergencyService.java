package com.era.emergency.service;

import com.era.emergency.dto.EmergencyDTOs.*;
import com.era.emergency.model.*;
import com.era.emergency.repository.EmergencyRequestRepository;
import com.era.emergency.service.AllocationEngine.AllocationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmergencyService {

    private final EmergencyRequestRepository emergencyRepo;
    private final AllocationEngine allocationEngine;

    // ─── Submit Emergency ─────────────────────────────────────────────────────

    @Transactional
    public EmergencyResponseDTO submitEmergency(EmergencyRequestDTO dto) {
        EmergencyRequest request = EmergencyRequest.builder()
                .patientName(dto.getPatientName())
                .patientAge(dto.getPatientAge())
                .severity(dto.getSeverity())
                .emergencyType(dto.getEmergencyType())
                .description(dto.getDescription())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .locationAddress(dto.getLocationAddress())
                .reporterContact(dto.getReporterContact())
                .status(EmergencyStatus.PENDING)
                .build();

        EmergencyRequest saved = emergencyRepo.save(request);
        log.info("Emergency submitted: id={}, severity={}, type={}", saved.getId(), saved.getSeverity(), saved.getEmergencyType());

        return EmergencyResponseDTO.builder()
                .emergencyId(saved.getId())
                .patientName(saved.getPatientName())
                .severity(saved.getSeverity())
                .emergencyType(saved.getEmergencyType())
                .status(saved.getStatus().name())
                .message("Emergency request received. Allocation in progress.")
                .createdAt(saved.getCreatedAt().toString())
                .build();
    }

    // ─── Allocate (single) ────────────────────────────────────────────────────

    @Transactional
    public AllocationResultDTO allocateEmergency(Long emergencyId) {
        EmergencyRequest emergency = emergencyRepo.findById(emergencyId)
                .orElseThrow(() -> new NoSuchElementException("Emergency not found with id: " + emergencyId));

        if (emergency.getStatus() == EmergencyStatus.ALLOCATED) {
            // Return the existing allocation
            return buildAllocationResult(emergency, null);
        }

        Optional<AllocationResult> best = allocationEngine.findBestHospital(emergency);

        if (best.isEmpty()) {
            throw new IllegalStateException("No suitable hospital found for emergency id: " + emergencyId);
        }

        AllocationResult result = best.get();
        Hospital hospital = result.getHospital();

        // Update the emergency record
        emergency.setStatus(EmergencyStatus.ALLOCATED);
        emergency.setAllocatedHospital(hospital);
        emergency.setAllocationScore(result.getTotalScore());
        emergency.setEstimatedDistanceKm(result.getDistanceKm());
        emergency.setEtaMinutes(result.getEtaMinutes());
        emergency.setAllocatedAt(LocalDateTime.now());
        emergencyRepo.save(emergency);

        // Decrement hospital resources
        decrementHospitalResources(hospital, emergency.getSeverity());

        log.info("Allocated emergency id={} → hospital '{}' (score={}, dist={}km, ETA={}min)",
                emergencyId, hospital.getName(), result.getTotalScore(),
                result.getDistanceKm(), result.getEtaMinutes());

        return buildAllocationResult(emergency, result);
    }

    // ─── Bulk Allocate All Pending ────────────────────────────────────────────

    @Transactional
    public List<AllocationResultDTO> allocateAllPending() {
        List<EmergencyRequest> pending = emergencyRepo.findPendingOrderedBySeverityAndTime();
        log.info("Bulk allocating {} pending emergencies", pending.size());
        return pending.stream()
                .map(e -> allocateEmergency(e.getId()))
                .toList();
    }

    // ─── Query ────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<EmergencyResponseDTO> getPendingEmergencies() {
        return emergencyRepo.findPendingOrderedBySeverityAndTime()
                .stream()
                .map(this::toEmergencyResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<EmergencyResponseDTO> getAllEmergencies() {
        return emergencyRepo.findAll().stream()
                .map(this::toEmergencyResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmergencyResponseDTO getEmergencyById(Long id) {
        EmergencyRequest e = emergencyRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Emergency not found: " + id));
        return toEmergencyResponseDTO(e);
    }

    // ─── Private Helpers ──────────────────────────────────────────────────────

    private void decrementHospitalResources(Hospital hospital, Severity severity) {
        if (severity == Severity.CRITICAL || severity == Severity.HIGH) {
            if (hospital.getAvailableIcuBeds() > 0)
                hospital.setAvailableIcuBeds(hospital.getAvailableIcuBeds() - 1);
        } else {
            if (hospital.getAvailableGeneralBeds() > 0)
                hospital.setAvailableGeneralBeds(hospital.getAvailableGeneralBeds() - 1);
        }
        if (hospital.getAvailableAmbulances() > 0)
            hospital.setAvailableAmbulances(hospital.getAvailableAmbulances() - 1);
    }

    private String buildAllocationReason(EmergencyRequest emergency, AllocationResult result) {
        if (result == null) return "Previously allocated";
        Hospital h = result.getHospital();
        return String.format(
                "Selected '%s' for %s emergency (score: %.2f). Distance: %.1f km, ETA: %d min. " +
                "Available ICU beds: %d, Ambulances: %d. %s",
                h.getName(), emergency.getSeverity(),
                result.getTotalScore(), result.getDistanceKm(), result.getEtaMinutes(),
                h.getAvailableIcuBeds(), h.getAvailableAmbulances(),
                h.isTraumaCenter() ? "Trauma centre: YES." : ""
        );
    }

    private AllocationResultDTO buildAllocationResult(EmergencyRequest emergency, AllocationResult result) {
        Hospital h = emergency.getAllocatedHospital();
        return AllocationResultDTO.builder()
                .emergencyId(emergency.getId())
                .patientName(emergency.getPatientName())
                .severity(emergency.getSeverity())
                .emergencyType(emergency.getEmergencyType())
                .hospitalId(h.getId())
                .hospitalName(h.getName())
                .hospitalAddress(h.getAddress())
                .hospitalCity(h.getCity())
                .traumaCenter(h.isTraumaCenter())
                .distanceKm(emergency.getEstimatedDistanceKm())
                .etaMinutes(emergency.getEtaMinutes())
                .allocationScore(emergency.getAllocationScore())
                .scoreBreakdown(result != null ? result.getScoreBreakdown() : "N/A")
                .allocationReason(buildAllocationReason(emergency, result))
                .allocatedAt(emergency.getAllocatedAt() != null ? emergency.getAllocatedAt().toString() : null)
                .build();
    }

    private EmergencyResponseDTO toEmergencyResponseDTO(EmergencyRequest e) {
        return EmergencyResponseDTO.builder()
                .emergencyId(e.getId())
                .patientName(e.getPatientName())
                .severity(e.getSeverity())
                .emergencyType(e.getEmergencyType())
                .status(e.getStatus().name())
                .message(e.getStatus() == EmergencyStatus.ALLOCATED
                        ? "Allocated to " + e.getAllocatedHospital().getName()
                        : "Pending allocation")
                .createdAt(e.getCreatedAt().toString())
                .build();
    }
}
