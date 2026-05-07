package com.insureth.insurance.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightInsuranceAdminPolicyRecordModel {
    private Long policyId;
    private String holder;
    private String riskKey;
    private String flightNumber;
    private String origin;
    private String destination;
    private Long departureTime;
    private Long purchaseTimestamp;
    private String premiumWei;
    private String payoutAmountWei;
    private Boolean active;
    private Boolean claimed;
    private Boolean resolved;
    private Boolean exists;
    private Boolean oracleRequested;
    private Integer statusInt;
    private String statusLabel;
    private Integer delayMinutes;
    private Integer policiesSharingRisk;
}
