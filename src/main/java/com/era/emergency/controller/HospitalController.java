package com.era.emergency.controller;

import com.era.emergency.dto.EmergencyDTOs.*;
import com.era.emergency.service.HospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for hospital management.
 */
@RestController
@RequestMapping("/hospital")
@RequiredArgsConstructor
@Slf4j
public class HospitalController {

    private final HospitalService hospitalService;

    /**
     * POST /hospital
     * Register a new hospital in the system.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<HospitalResponseDTO>> registerHospital(
            @Valid @RequestBody HospitalRequestDTO dto) {
        HospitalResponseDTO result = hospitalService.registerHospital(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hospital registered successfully", result));
    }

    /**
     * GET /hospital
     * List all hospitals (active + inactive).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<HospitalResponseDTO>>> getAllHospitals() {
        List<HospitalResponseDTO> hospitals = hospitalService.getAllHospitals();
        return ResponseEntity.ok(ApiResponse.success("Fetched " + hospitals.size() + " hospitals", hospitals));
    }

    /**
     * GET /hospital/active
     * List only active hospitals.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<HospitalResponseDTO>>> getActiveHospitals() {
        List<HospitalResponseDTO> hospitals = hospitalService.getActiveHospitals();
        return ResponseEntity.ok(ApiResponse.success("Fetched " + hospitals.size() + " active hospitals", hospitals));
    }

    /**
     * GET /hospital/{id}
     * Fetch a hospital by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<HospitalResponseDTO>> getHospitalById(@PathVariable Long id) {
        HospitalResponseDTO hospital = hospitalService.getHospitalById(id);
        return ResponseEntity.ok(ApiResponse.success("Hospital found", hospital));
    }

    /**
     * PUT /hospital/{id}
     * Update an existing hospital.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<HospitalResponseDTO>> updateHospital(
            @PathVariable Long id,
            @Valid @RequestBody HospitalRequestDTO dto) {
        HospitalResponseDTO updated = hospitalService.updateHospital(id, dto);
        return ResponseEntity.ok(ApiResponse.success("Hospital updated successfully", updated));
    }
}
