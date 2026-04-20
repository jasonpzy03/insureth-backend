package com.insureth.insurance.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "flight_insurance_policy")
public class FlightInsurancePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "projection_id")
    private Long projectionId;

    @Column(name = "policy_id", nullable = false, unique = true)
    private Long policyId;

    @Column(name = "holder", nullable = false, length = 100)
    private String holder;

    @Column(name = "risk_key", nullable = false, length = 66)
    private String riskKey;

    @Column(name = "flight_number", nullable = false, length = 50)
    private String flightNumber;

    @Column(name = "origin", nullable = false, length = 10)
    private String origin;

    @Column(name = "destination", nullable = false, length = 10)
    private String destination;

    @Column(name = "departure_time", nullable = false)
    private Long departureTime;

    @Column(name = "purchase_timestamp")
    private Long purchaseTimestamp;

    @Column(name = "premium_wei", nullable = false, length = 120)
    private String premiumWei;

    @Column(name = "payout_amount_wei", nullable = false, length = 120)
    private String payoutAmountWei;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "claimed", nullable = false)
    private Boolean claimed;

    @Column(name = "resolved", nullable = false)
    private Boolean resolved;

    @Column(name = "exists_on_chain", nullable = false)
    private Boolean existsOnChain;

    @Column(name = "oracle_requested", nullable = false)
    private Boolean oracleRequested;

    @Column(name = "status_int", nullable = false)
    private Integer statusInt;

    @Column(name = "status_label", nullable = false, length = 50)
    private String statusLabel;

    @Column(name = "delay_minutes", nullable = false)
    private Integer delayMinutes;

    @Column(name = "policies_sharing_risk", nullable = false)
    private Integer policiesSharingRisk;

    @Column(name = "last_synced_at", nullable = false)
    private Instant lastSyncedAt;
}
