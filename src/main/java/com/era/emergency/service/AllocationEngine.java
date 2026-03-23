package com.era.emergency.service;

import com.era.emergency.model.*;
import com.era.emergency.repository.HospitalRepository;
import com.era.emergency.util.GeoDistanceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Core AI-based allocation engine.
 *
 * Scoring formula:
 *   score = (W_distance × distanceScore) + (W_severity × severityScore) + (W_availability × availabilityScore)
 *
 * All weights are configurable via application.properties.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AllocationEngine {

    private final HospitalRepository hospitalRepository;
    private final GeoDistanceUtil geoUtil;

    @Value("${era.allocation.max-distance-km:100.0}")
    private double maxDistanceKm;

    @Value("${era.allocation.weight.distance:0.35}")
    private double weightDistance;

    @Value("${era.allocation.weight.severity:0.45}")
    private double weightSeverity;

    @Value("${era.allocation.weight.availability:0.20}")
    private double weightAvailability;

    // ─── Public API ───────────────────────────────────────────────────────────

    /**
     * Find the best hospital for the given emergency request.
     *
     * @return Optional containing the best-match hospital or empty if none suitable.
     */
    public Optional<AllocationResult> findBestHospital(EmergencyRequest emergency) {
        List<Hospital> candidates = hospitalRepository.findAvailableHospitals();

        if (candidates.isEmpty()) {
            log.warn("No available hospitals found for emergency id={}", emergency.getId());
            return Optional.empty();
        }

        // For CRITICAL severity: prefer trauma centres
        if (emergency.getSeverity() == Severity.CRITICAL) {
            List<Hospital> traumaCentres = candidates.stream()
                    .filter(Hospital::isTraumaCenter)
                    .toList();
            if (!traumaCentres.isEmpty()) {
                candidates = traumaCentres;
                log.info("CRITICAL case – restricting candidates to {} trauma centres", traumaCentres.size());
            }
        }

        return candidates.stream()
                .map(h -> score(emergency, h))
                .filter(r -> r.getDistanceKm() <= maxDistanceKm)
                .max(Comparator.comparingDouble(AllocationResult::getTotalScore));
    }

    // ─── Scoring ──────────────────────────────────────────────────────────────

    private AllocationResult score(EmergencyRequest emergency, Hospital hospital) {
        double distKm = geoUtil.calculateDistanceKm(
                emergency.getLatitude(), emergency.getLongitude(),
                hospital.getLatitude(), hospital.getLongitude()
        );

        double distScore       = geoUtil.normaliseDistanceScore(distKm, maxDistanceKm);
        double severityScore   = computeSeverityScore(emergency.getSeverity(), hospital);
        double availScore      = hospital.getCapacityScore();

        // Ambulance availability bonus
        double ambulanceBonus  = hospital.getAvailableAmbulances() > 0 ? 0.05 : 0.0;

        double total = (weightDistance * distScore)
                     + (weightSeverity * severityScore)
                     + (weightAvailability * availScore)
                     + ambulanceBonus;

        int eta = geoUtil.estimateEtaMinutes(distKm);

        String breakdown = String.format(
                "distance=%.2f(×%.2f) + severity=%.2f(×%.2f) + availability=%.2f(×%.2f) + ambulance=%.2f",
                distScore, weightDistance,
                severityScore, weightSeverity,
                availScore, weightAvailability,
                ambulanceBonus
        );

        return AllocationResult.builder()
                .hospital(hospital)
                .distanceKm(distKm)
                .etaMinutes(eta)
                .distanceScore(distScore)
                .severityScore(severityScore)
                .availabilityScore(availScore)
                .totalScore(Math.min(total, 1.0))
                .scoreBreakdown(breakdown)
                .build();
    }

    /**
     * Severity score accounts for both the case urgency and the hospital's ICU capability.
     */
    private double computeSeverityScore(Severity severity, Hospital hospital) {
        double icuRatio  = hospital.getIcuAvailabilityRatio();
        double genRatio  = hospital.getCapacityScore();

        return switch (severity) {
            case CRITICAL -> {
                // ICU availability matters most
                double base = 0.5 + (0.5 * icuRatio);
                yield hospital.isTraumaCenter() ? Math.min(base + 0.2, 1.0) : base;
            }
            case HIGH -> 0.4 + (0.4 * icuRatio) + (0.2 * genRatio);
            case MEDIUM -> 0.3 + (0.3 * icuRatio) + (0.4 * genRatio);
            case LOW -> 0.2 + (0.8 * genRatio);
        };
    }

    // ─── Inner Result DTO ─────────────────────────────────────────────────────

    @lombok.Data @lombok.Builder
    public static class AllocationResult {
        private Hospital hospital;
        private double distanceKm;
        private int etaMinutes;
        private double distanceScore;
        private double severityScore;
        private double availabilityScore;
        private double totalScore;
        private String scoreBreakdown;
    }
}
