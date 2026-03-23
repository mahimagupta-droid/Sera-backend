package com.era.emergency.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Patient name is required")
    @Column(name = "patient_name", nullable = false)
    private String patientName;

    @Column(name = "patient_age")
    @Min(0) @Max(150)
    private Integer patientAge;

    @NotNull(message = "Severity is required")
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @NotBlank(message = "Emergency type is required")
    @Column(name = "emergency_type")
    private String emergencyType;       // e.g. "Cardiac Arrest", "Road Accident"

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Latitude of the emergency location */
    @NotNull(message = "Latitude is required")
    @DecimalMin("-90.0") @DecimalMax("90.0")
    private Double latitude;

    /** Longitude of the emergency location */
    @NotNull(message = "Longitude is required")
    @DecimalMin("-180.0") @DecimalMax("180.0")
    private Double longitude;

    @Column(name = "location_address")
    private String locationAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private EmergencyStatus status = EmergencyStatus.PENDING;

    /** The hospital this request was allocated to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allocated_hospital_id")
    private Hospital allocatedHospital;

    /** Allocation score computed at time of allocation */
    @Column(name = "allocation_score")
    private Double allocationScore;

    /** Estimated distance to allocated hospital in km */
    @Column(name = "estimated_distance_km")
    private Double estimatedDistanceKm;

    /** Estimated time of arrival in minutes */
    @Column(name = "eta_minutes")
    private Integer etaMinutes;

    @Column(name = "reporter_contact")
    private String reporterContact;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "allocated_at")
    private LocalDateTime allocatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
