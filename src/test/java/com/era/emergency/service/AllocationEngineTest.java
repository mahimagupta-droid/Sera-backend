package com.era.emergency.service;

import com.era.emergency.model.*;
import com.era.emergency.repository.HospitalRepository;
import com.era.emergency.util.GeoDistanceUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AllocationEngineTest {

    @Mock  HospitalRepository hospitalRepository;
    @InjectMocks AllocationEngine engine;

    private final GeoDistanceUtil geoUtil = new GeoDistanceUtil();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(engine, "geoUtil",        geoUtil);
        ReflectionTestUtils.setField(engine, "maxDistanceKm",  100.0);
        ReflectionTestUtils.setField(engine, "weightDistance", 0.35);
        ReflectionTestUtils.setField(engine, "weightSeverity", 0.45);
        ReflectionTestUtils.setField(engine, "weightAvailability", 0.20);
    }

    // ─── Helper builders ────────────────────────────────────────────────────

    private Hospital buildHospital(Long id, String name, double lat, double lon,
                                   int icuTotal, int icuAvail,
                                   int genTotal, int genAvail,
                                   int ambulances, boolean trauma) {
        return Hospital.builder()
                .id(id).name(name)
                .latitude(lat).longitude(lon)
                .totalIcuBeds(icuTotal).availableIcuBeds(icuAvail)
                .totalGeneralBeds(genTotal).availableGeneralBeds(genAvail)
                .availableAmbulances(ambulances)
                .isActive(true).traumaCenter(trauma)
                .build();
    }

    private EmergencyRequest buildEmergency(Severity severity, double lat, double lon) {
        return EmergencyRequest.builder()
                .id(1L).patientName("Test Patient")
                .severity(severity).emergencyType("Test")
                .latitude(lat).longitude(lon)
                .status(EmergencyStatus.PENDING)
                .build();
    }

    // ─── Tests ──────────────────────────────────────────────────────────────

    @Test
    void shouldReturnEmptyWhenNoHospitalsAvailable() {
        when(hospitalRepository.findAvailableHospitals()).thenReturn(List.of());
        EmergencyRequest emergency = buildEmergency(Severity.HIGH, 28.55, 77.25);

        Optional<AllocationEngine.AllocationResult> result = engine.findBestHospital(emergency);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldPreferCloserHospitalForLowSeverity() {
        Hospital near = buildHospital(1L, "Near Hospital", 28.56, 77.26,
                10, 5, 100, 50, 3, false);
        Hospital far  = buildHospital(2L, "Far Hospital",  28.70, 77.40,
                10, 9, 100, 90, 5, false);

        when(hospitalRepository.findAvailableHospitals()).thenReturn(List.of(near, far));

        EmergencyRequest emergency = buildEmergency(Severity.LOW, 28.55, 77.25);
        Optional<AllocationEngine.AllocationResult> result = engine.findBestHospital(emergency);

        assertThat(result).isPresent();
        // Near hospital should win for LOW severity because distance weight is significant
        assertThat(result.get().getHospital().getName()).isEqualTo("Near Hospital");
    }

    @Test
    void shouldPreferTraumaCentreForCriticalEmergency() {
        Hospital regular = buildHospital(1L, "Regular Hospital", 28.56, 77.26,
                10, 8, 100, 80, 3, false);
        Hospital trauma  = buildHospital(2L, "Trauma Centre",    28.60, 77.28,
                20, 15, 150, 100, 5, true);

        // For CRITICAL, engine filters to trauma centres first
        when(hospitalRepository.findAvailableHospitals()).thenReturn(List.of(regular, trauma));

        EmergencyRequest emergency = buildEmergency(Severity.CRITICAL, 28.55, 77.25);
        Optional<AllocationEngine.AllocationResult> result = engine.findBestHospital(emergency);

        assertThat(result).isPresent();
        assertThat(result.get().getHospital().isTraumaCenter()).isTrue();
        assertThat(result.get().getHospital().getName()).isEqualTo("Trauma Centre");
    }

    @Test
    void shouldExcludeHospitalsBeyondMaxDistance() {
        // Hospital placed > 100 km away
        Hospital distant = buildHospital(1L, "Distant Hospital", 29.60, 78.50,
                10, 10, 100, 100, 5, true);

        when(hospitalRepository.findAvailableHospitals()).thenReturn(List.of(distant));

        EmergencyRequest emergency = buildEmergency(Severity.HIGH, 28.55, 77.25);
        Optional<AllocationEngine.AllocationResult> result = engine.findBestHospital(emergency);

        assertThat(result).isEmpty();
    }

    @Test
    void allocationScoreShouldBeBetweenZeroAndOne() {
        Hospital h = buildHospital(1L, "Hospital A", 28.57, 77.27,
                20, 10, 200, 100, 4, false);

        when(hospitalRepository.findAvailableHospitals()).thenReturn(List.of(h));

        EmergencyRequest emergency = buildEmergency(Severity.MEDIUM, 28.55, 77.25);
        Optional<AllocationEngine.AllocationResult> result = engine.findBestHospital(emergency);

        assertThat(result).isPresent();
        double score = result.get().getTotalScore();
        assertThat(score).isBetween(0.0, 1.0);
    }

    @Test
    void shouldFallbackToNonTraumaCentreIfNoneAvailableForCritical() {
        Hospital regular = buildHospital(1L, "Regular Hospital", 28.56, 77.26,
                10, 8, 100, 80, 3, false);

        when(hospitalRepository.findAvailableHospitals()).thenReturn(List.of(regular));

        EmergencyRequest emergency = buildEmergency(Severity.CRITICAL, 28.55, 77.25);
        Optional<AllocationEngine.AllocationResult> result = engine.findBestHospital(emergency);

        // Falls back to the only available hospital even if not trauma
        assertThat(result).isPresent();
        assertThat(result.get().getHospital().getName()).isEqualTo("Regular Hospital");
    }

    @Test
    void etaShouldIncludeDispatchOverhead() {
        // 10 km away → ~12 min travel at 50 km/h + 5 min overhead = 17 min
        Hospital h = buildHospital(1L, "Close Hospital", 28.6394, 77.3114,
                10, 5, 100, 50, 2, false);

        when(hospitalRepository.findAvailableHospitals()).thenReturn(List.of(h));
        // Emergency at Noida sector 18 (~10 km from hospital above)
        EmergencyRequest emergency = buildEmergency(Severity.MEDIUM, 28.5700, 77.3210);
        Optional<AllocationEngine.AllocationResult> result = engine.findBestHospital(emergency);

        assertThat(result).isPresent();
        assertThat(result.get().getEtaMinutes()).isGreaterThanOrEqualTo(5); // at least overhead
    }
}
