package com.insureth.insurance.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyResolutionNotificationRequest {
    private String policyId;
    private String flightNumber;
    private String origin;
    private String destination;
    private String departureAirportName;
    private String arrivalAirportName;
    private String status;
    private String delayMinutes;
    private String payoutEth;
    private String resolutionTime;
    private String transactionHash;
    private boolean hasPayout;
}
