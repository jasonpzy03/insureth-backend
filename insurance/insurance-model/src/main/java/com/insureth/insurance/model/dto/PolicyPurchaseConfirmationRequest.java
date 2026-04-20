package com.insureth.insurance.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyPurchaseConfirmationRequest {

    private String policyId;

    @NotBlank
    private String flightNumber;

    @NotBlank
    private String departureAirportIata;

    @NotBlank
    private String arrivalAirportIata;

    @NotBlank
    private String departureAirportName;

    @NotBlank
    private String arrivalAirportName;

    @NotBlank
    private String departureTime;

    @NotBlank
    private String premiumPaidEth;

    @NotBlank
    private String netPremiumEth;

    @NotBlank
    private String platformFeeEth;

    @NotBlank
    private String currency;

    @NotBlank
    private String transactionHash;
}
