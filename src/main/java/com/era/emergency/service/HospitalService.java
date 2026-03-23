package com.era.emergency.service;

import com.era.emergency.dto.EmergencyDTOs.*;
import com.era.emergency.model.Hospital;
import com.era.emergency.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    // ─── Create ──────────────────────────────────────────────────────────────

    @Transactional
    public HospitalResponseDTO registerHospital(HospitalRequestDTO dto) {
        Hospital hospital = Hospital.builder()
                .name(dto.getName())
                .address(dto.getAddress())
                .city(dto.getCity())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .totalIcuBeds(dto.getTotalIcuBeds())
                .availableIcuBeds(dto.getAvailableIcuBeds())
                .totalGeneralBeds(dto.getTotalGeneralBeds())
                .availableGeneralBeds(dto.getAvailableGeneralBeds())
                .availableAmbulances(dto.getAvailableAmbulances())
                .specializations(dto.getSpecializations())
                .isActive(dto.isActive())
                .traumaCenter(dto.isTraumaCenter())
                .build();

        Hospital saved = hospitalRepository.save(hospital);
        log.info("Registered hospital: {} (id={})", saved.getName(), saved.getId());
        return toResponseDTO(saved);
    }

    // ─── Read ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<HospitalResponseDTO> getAllHospitals() {
        return hospitalRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<HospitalResponseDTO> getActiveHospitals() {
        return hospitalRepository.findByIsActiveTrue().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public HospitalResponseDTO getHospitalById(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Hospital not found with id: " + id));
        return toResponseDTO(hospital);
    }

    // ─── Update ──────────────────────────────────────────────────────────────

    @Transactional
    public HospitalResponseDTO updateHospital(Long id, HospitalRequestDTO dto) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Hospital not found with id: " + id));

        hospital.setName(dto.getName());
        hospital.setAddress(dto.getAddress());
        hospital.setCity(dto.getCity());
        hospital.setLatitude(dto.getLatitude());
        hospital.setLongitude(dto.getLongitude());
        hospital.setTotalIcuBeds(dto.getTotalIcuBeds());
        hospital.setAvailableIcuBeds(dto.getAvailableIcuBeds());
        hospital.setTotalGeneralBeds(dto.getTotalGeneralBeds());
        hospital.setAvailableGeneralBeds(dto.getAvailableGeneralBeds());
        hospital.setAvailableAmbulances(dto.getAvailableAmbulances());
        hospital.setSpecializations(dto.getSpecializations());
        hospital.setActive(dto.isActive());
        hospital.setTraumaCenter(dto.isTraumaCenter());

        return toResponseDTO(hospitalRepository.save(hospital));
    }

    // ─── Mapper ──────────────────────────────────────────────────────────────

    public HospitalResponseDTO toResponseDTO(Hospital h) {
        return HospitalResponseDTO.builder()
                .id(h.getId())
                .name(h.getName())
                .address(h.getAddress())
                .city(h.getCity())
                .latitude(h.getLatitude())
                .longitude(h.getLongitude())
                .totalIcuBeds(h.getTotalIcuBeds())
                .availableIcuBeds(h.getAvailableIcuBeds())
                .totalGeneralBeds(h.getTotalGeneralBeds())
                .availableGeneralBeds(h.getAvailableGeneralBeds())
                .availableAmbulances(h.getAvailableAmbulances())
                .specializations(h.getSpecializations())
                .isActive(h.isActive())
                .traumaCenter(h.isTraumaCenter())
                .capacityScore(h.getCapacityScore())
                .createdAt(h.getCreatedAt() != null ? h.getCreatedAt().toString() : null)
                .build();
    }
}
