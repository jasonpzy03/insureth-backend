package com.insureth.insurance.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flight_insurance_notification_sync_state")
public class FlightInsuranceNotificationSyncState {

    @Id
    @Column(name = "sync_key", nullable = false, length = 100)
    private String syncKey;

    @Column(name = "last_processed_block", nullable = false)
    private Long lastProcessedBlock;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
