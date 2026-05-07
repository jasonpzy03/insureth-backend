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
    private BigDecimal premiumBaseRate;
    private BigDecimal premiumPerDayMultiplier;
    private BigDecimal premiumDemandMultiplier;
    private BigDecimal premiumMaxMultiplier;
    private String currency;
}
