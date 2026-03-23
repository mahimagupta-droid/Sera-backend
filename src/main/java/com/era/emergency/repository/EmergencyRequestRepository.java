package com.era.emergency.repository;

import com.era.emergency.model.EmergencyRequest;
import com.era.emergency.model.EmergencyStatus;
import com.era.emergency.model.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, Long> {

    List<EmergencyRequest> findByStatus(EmergencyStatus status);

    List<EmergencyRequest> findBySeverity(Severity severity);

    List<EmergencyRequest> findByStatusOrderByCreatedAtDesc(EmergencyStatus status);

    @Query("SELECT e FROM EmergencyRequest e WHERE e.status = 'PENDING' ORDER BY e.severity DESC, e.createdAt ASC")
    List<EmergencyRequest> findPendingOrderedBySeverityAndTime();

    @Query("SELECT COUNT(e) FROM EmergencyRequest e WHERE e.status = :status")
    long countByStatus(EmergencyStatus status);

    List<EmergencyRequest> findByAllocatedHospitalId(Long hospitalId);
}
