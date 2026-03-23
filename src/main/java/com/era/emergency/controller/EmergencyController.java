package com.era.emergency.controller;

import com.era.emergency.dto.EmergencyDTOs.*;
import com.era.emergency.service.EmergencyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for emergency request lifecycle and allocation.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class EmergencyController {

    private final EmergencyService emergencyService;

    // ──────────────────────────────────────────────────────────────────────────
    // POST /emergency  – Submit a new emergency
    // ──────────────────────────────────────────────────────────────────────────
    @PostMapping("/emergency")
    public ResponseEntity<ApiResponse<EmergencyResponseDTO>> submitEmergency(
            @Valid @RequestBody EmergencyRequestDTO dto) {

        log.info("Incoming emergency: patient={}, severity={}", dto.getPatientName(), dto.getSeverity());
        EmergencyResponseDTO result = emergencyService.submitEmergency(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Emergency submitted. Awaiting allocation.", result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /allocate  – Trigger allocation for all pending emergencies
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/allocate")
    public ResponseEntity<ApiResponse<List<AllocationResultDTO>>> allocateAllPending() {
        log.info("Allocation triggered for all pending emergencies");
        List<AllocationResultDTO> results = emergencyService.allocateAllPending();
        String msg = results.isEmpty()
                ? "No pending emergencies to allocate"
                : "Allocated " + results.size() + " emergencies";
        return ResponseEntity.ok(ApiResponse.success(msg, results));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /allocate/{id}  – Allocate a specific emergency by ID
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/allocate/{id}")
    public ResponseEntity<ApiResponse<AllocationResultDTO>> allocateSingle(@PathVariable Long id) {
        AllocationResultDTO result = emergencyService.allocateEmergency(id);
        return ResponseEntity.ok(ApiResponse.success("Allocation successful", result));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /emergency  – List all emergencies
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/emergency")
    public ResponseEntity<ApiResponse<List<EmergencyResponseDTO>>> getAllEmergencies() {
        List<EmergencyResponseDTO> list = emergencyService.getAllEmergencies();
        return ResponseEntity.ok(ApiResponse.success("Fetched " + list.size() + " emergencies", list));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /emergency/pending  – List only PENDING emergencies
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/emergency/pending")
    public ResponseEntity<ApiResponse<List<EmergencyResponseDTO>>> getPendingEmergencies() {
        List<EmergencyResponseDTO> list = emergencyService.getPendingEmergencies();
        return ResponseEntity.ok(ApiResponse.success(list.size() + " pending emergencies", list));
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /emergency/{id}  – Get a single emergency by ID
    // ──────────────────────────────────────────────────────────────────────────
    @GetMapping("/emergency/{id}")
    public ResponseEntity<ApiResponse<EmergencyResponseDTO>> getEmergencyById(@PathVariable Long id) {
        EmergencyResponseDTO result = emergencyService.getEmergencyById(id);
        return ResponseEntity.ok(ApiResponse.success("Emergency found", result));
    }
}
