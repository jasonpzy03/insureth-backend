package com.insureth.insurance.model.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightInsuranceProductConfigModel {
    private String productCode;
    private BigDecimal basePremiumEth;
    private BigDecimal delayPayoutTier1Eth;
    private BigDecimal delayPayoutTier2Eth;
    private BigDecimal delayPayoutTier3Eth;
    private BigDecimal cancellationPayoutEth;
    private Integer delayThresholdTier1Minutes;
    private Integer delayThresholdTier2Minutes;
    private Integer delayThresholdTier3Minutes;
    private BigDecimal premiumBaseRate;
    private BigDecimal premiumPerDayMultiplier;
    private BigDecimal premiumDemandMultiplier;
    private BigDecimal premiumMaxMultiplier;
    private String currency;
}
