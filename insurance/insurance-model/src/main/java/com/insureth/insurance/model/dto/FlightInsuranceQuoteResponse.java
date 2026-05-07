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
public class FlightInsuranceQuoteResponse {
    private String airlineIataCode;
    private String flightNumber;
    private String quoteSource;
    private Integer daysUntilDeparture;
    private BigDecimal basePremiumEth;
    private BigDecimal daysMultiplier;
    private BigDecimal performanceMultiplier;
    private BigDecimal totalMultiplier;
    private BigDecimal quotedPremiumEth;
    private BigDecimal ontimePercent;
    private BigDecimal delayMeanMinutes;
    private BigDecimal allStars;
    private BigDecimal severeDelayRate;
    private BigDecimal disruptionRate;
}
