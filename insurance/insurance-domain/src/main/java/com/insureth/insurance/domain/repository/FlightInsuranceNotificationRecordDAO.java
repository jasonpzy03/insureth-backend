package com.insureth.insurance.domain.repository;

import com.insureth.insurance.domain.entity.FlightInsuranceNotificationRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightInsuranceNotificationRecordDAO extends JpaRepository<FlightInsuranceNotificationRecord, Long> {
    Optional<FlightInsuranceNotificationRecord> findByEventKey(String eventKey);
    List<FlightInsuranceNotificationRecord> findByHolderIgnoreCaseOrderByEventTimestampDescCreatedAtDesc(
            String holder,
            Pageable pageable
    );
    Optional<FlightInsuranceNotificationRecord> findFirstByPolicyIdAndTypeOrderByEventTimestampDescCreatedAtDesc(
            Long policyId,
            String type
    );
    List<FlightInsuranceNotificationRecord> findByRiskKeyAndType(String riskKey, String type);
}
