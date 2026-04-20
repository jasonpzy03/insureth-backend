package com.insureth.insurance.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
@Table(
        name = "flight_insurance_notification_record",
        indexes = {
                @Index(name = "idx_flight_insurance_notification_holder", columnList = "holder"),
                @Index(name = "idx_flight_insurance_notification_policy", columnList = "policy_id")
        }
)
public class FlightInsuranceNotificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "event_key", nullable = false, unique = true, length = 160)
    private String eventKey;

    @Column(name = "holder", nullable = false, length = 100)
    private String holder;

    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @Column(name = "risk_key", length = 66)
    private String riskKey;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Column(name = "amount_wei", length = 120)
    private String amountWei;

    @Column(name = "flight_number", length = 50)
    private String flightNumber;

    @Column(name = "origin", length = 10)
    private String origin;

    @Column(name = "destination", length = 10)
    private String destination;

    @Column(name = "departure_time")
    private Long departureTime;

    @Column(name = "transaction_hash", nullable = false, length = 66)
    private String transactionHash;

    @Column(name = "block_number", nullable = false)
    private Long blockNumber;

    @Column(name = "log_index", nullable = false)
    private Integer logIndex;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
