package com.era.emergency.dto;

import com.era.emergency.model.Severity;
import jakarta.validation.constraints.*;
import lombok.*;

// ─── Inbound DTOs ────────────────────────────────────────────────────────────

public class EmergencyDTOs {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EmergencyRequestDTO {
        @NotBlank(message = "Patient name is required")
        private String patientName;

        @Min(0) @Max(150)
        private Integer patientAge;

        @NotNull(message = "Severity is required")
        private Severity severity;

        @NotBlank(message = "Emergency type is required")
        private String emergencyType;

        private String description;

        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
        private Double latitude;

        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
        private Double longitude;

        private String locationAddress;
        private String reporterContact;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class HospitalRequestDTO {
        @NotBlank private String name;
        @NotBlank private String address;
        @NotBlank private String city;

        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
        private Double latitude;

        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
        private Double longitude;

        @Min(0) private int totalIcuBeds;
        @Min(0) private int availableIcuBeds;
        @Min(0) private int totalGeneralBeds;
        @Min(0) private int availableGeneralBeds;
        @Min(0) private int availableAmbulances;
        private String specializations;
        private boolean isActive;
        private boolean traumaCenter;
    }

// ─── Outbound DTOs ───────────────────────────────────────────────────────────

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class EmergencyResponseDTO {
        private Long emergencyId;
        private String patientName;
        private Severity severity;
        private String emergencyType;
        private String status;
        private String message;
        private String createdAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AllocationResultDTO {
        private Long emergencyId;
        private String patientName;
        private Severity severity;
        private String emergencyType;

        private Long hospitalId;
        private String hospitalName;
        private String hospitalAddress;
        private String hospitalCity;
        private boolean traumaCenter;

        private double distanceKm;
        private int etaMinutes;
        private double allocationScore;

        private String scoreBreakdown;
        private String allocationReason;
        private String allocatedAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class HospitalResponseDTO {
        private Long id;
        private String name;
        private String address;
        private String city;
        private Double latitude;
        private Double longitude;
        private int totalIcuBeds;
        private int availableIcuBeds;
        private int totalGeneralBeds;
        private int availableGeneralBeds;
        private int availableAmbulances;
        private String specializations;
        private boolean isActive;
        private boolean traumaCenter;
        private double capacityScore;
        private String createdAt;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;
        private String timestamp;

        public static <T> ApiResponse<T> success(String message, T data) {
            return ApiResponse.<T>builder()
                    .success(true)
                    .message(message)
                    .data(data)
                    .timestamp(java.time.LocalDateTime.now().toString())
                    .build();
        }

        public static <T> ApiResponse<T> error(String message) {
            return ApiResponse.<T>builder()
                    .success(false)
                    .message(message)
                    .timestamp(java.time.LocalDateTime.now().toString())
                    .build();
        }
    }
}
