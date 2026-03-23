package com.era.emergency.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "hospitals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Hospital name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "City is required")
    private String city;

    /** Latitude coordinate of the hospital */
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private Double latitude;

    /** Longitude coordinate of the hospital */
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private Double longitude;

    /** Total ICU beds in the hospital */
    @Min(0)
    @Column(name = "total_icu_beds")
    private int totalIcuBeds;

    /** Currently available ICU beds */
    @Min(0)
    @Column(name = "available_icu_beds")
    private int availableIcuBeds;

    /** Total general beds */
    @Min(0)
    @Column(name = "total_general_beds")
    private int totalGeneralBeds;

    /** Currently available general beds */
    @Min(0)
    @Column(name = "available_general_beds")
    private int availableGeneralBeds;

    /** Number of ambulances currently available */
    @Min(0)
    @Column(name = "available_ambulances")
    private int availableAmbulances;

    /** Specializations this hospital supports (comma-separated) */
    private String specializations;

    /** Whether the hospital is currently accepting emergencies */
    @Column(name = "is_active")
    private boolean isActive;

    @Column(name = "trauma_center")
    private boolean traumaCenter;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /** Computed availability ratio: available ICU / total ICU */
    @Transient
    public double getIcuAvailabilityRatio() {
        if (totalIcuBeds == 0) return 0.0;
        return (double) availableIcuBeds / totalIcuBeds;
    }

    /** Computed overall capacity score (0.0 – 1.0) */
    @Transient
    public double getCapacityScore() {
        int totalBeds = totalIcuBeds + totalGeneralBeds;
        int availableBeds = availableIcuBeds + availableGeneralBeds;
        if (totalBeds == 0) return 0.0;
        return (double) availableBeds / totalBeds;
    }
}
