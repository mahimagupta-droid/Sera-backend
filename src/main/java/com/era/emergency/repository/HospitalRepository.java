package com.era.emergency.repository;

import com.era.emergency.model.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    List<Hospital> findByIsActiveTrue();

    List<Hospital> findByIsActiveTrueAndAvailableIcuBedsGreaterThan(int minBeds);

    List<Hospital> findByIsActiveTrueAndAvailableGeneralBedsGreaterThan(int minBeds);

    @Query("SELECT h FROM Hospital h WHERE h.isActive = true AND h.availableAmbulances > 0")
    List<Hospital> findActiveHospitalsWithAmbulances();

    @Query("SELECT h FROM Hospital h WHERE h.isActive = true AND " +
           "(h.availableIcuBeds > 0 OR h.availableGeneralBeds > 0) AND " +
           "h.availableAmbulances > 0")
    List<Hospital> findAvailableHospitals();

    List<Hospital> findByCity(String city);

    @Query("SELECT h FROM Hospital h WHERE h.isActive = true AND h.traumaCenter = true")
    List<Hospital> findActiveTraumaCenters();
}
