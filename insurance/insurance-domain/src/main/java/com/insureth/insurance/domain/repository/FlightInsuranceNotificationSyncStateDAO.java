package com.insureth.insurance.domain.repository;

import com.insureth.insurance.domain.entity.FlightInsuranceNotificationSyncState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlightInsuranceNotificationSyncStateDAO extends JpaRepository<FlightInsuranceNotificationSyncState, String> {
}
