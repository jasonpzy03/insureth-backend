package com.insureth.insurance.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "flight_insurance_product_config")
public class FlightInsuranceProductConfig {

    @Id
    @Column(name = "config_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long configId;

    @Column(name = "product_code", nullable = false, unique = true)
    private String productCode;

    @Column(name = "base_premium_eth", nullable = false, precision = 18, scale = 6)
    private BigDecimal basePremiumEth;

    @Column(name = "delay_payout_tier1_eth", nullable = false, precision = 18, scale = 6)
    private BigDecimal delayPayoutTier1Eth;

    @Column(name = "delay_payout_tier2_eth", nullable = false, precision = 18, scale = 6)
    private BigDecimal delayPayoutTier2Eth;

    @Column(name = "delay_payout_tier3_eth", nullable = false, precision = 18, scale = 6)
    private BigDecimal delayPayoutTier3Eth;

    @Column(name = "cancellation_payout_eth", nullable = false, precision = 18, scale = 6)
    private BigDecimal cancellationPayoutEth;

    @Column(name = "delay_threshold_tier1_minutes", nullable = false)
    private Integer delayThresholdTier1Minutes;

    @Column(name = "delay_threshold_tier2_minutes", nullable = false)
    private Integer delayThresholdTier2Minutes;

    @Column(name = "delay_threshold_tier3_minutes", nullable = false)
    private Integer delayThresholdTier3Minutes;

    @Column(name = "premium_base_rate", nullable = false, precision = 12, scale = 6)
    private BigDecimal premiumBaseRate;

    @Column(name = "premium_per_day_multiplier", nullable = false, precision = 12, scale = 6)
    private BigDecimal premiumPerDayMultiplier;

    @Column(name = "premium_demand_multiplier", nullable = false, precision = 12, scale = 6)
    private BigDecimal premiumDemandMultiplier;

    @Column(name = "premium_max_multiplier", nullable = false, precision = 12, scale = 6)
    private BigDecimal premiumMaxMultiplier;

    @Column(name = "currency", nullable = false)
    private String currency;
}
