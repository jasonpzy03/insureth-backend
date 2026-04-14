package com.insureth.insurance.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
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
        name = "flight_insurance_flight_rating",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_flight_insurance_flight_rating_airline_flight",
                columnNames = {"airline_code", "flight_number"}
        )
)
public class FlightInsuranceFlightRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    private Long ratingId;

    @Column(name = "airline_code", nullable = false)
    private String airlineCode;

    @Column(name = "flight_number", nullable = false)
    private String flightNumber;

    @Column(name = "airline_name")
    private String airlineName;

    @Column(name = "departure_airport_code")
    private String departureAirportCode;

    @Column(name = "departure_airport_name")
    private String departureAirportName;

    @Column(name = "arrival_airport_code")
    private String arrivalAirportCode;

    @Column(name = "arrival_airport_name")
    private String arrivalAirportName;

    @Column(name = "observations")
    private Integer observations;

    @Column(name = "ontime")
    private Integer ontime;

    @Column(name = "late15")
    private Integer late15;

    @Column(name = "late30")
    private Integer late30;

    @Column(name = "late45")
    private Integer late45;

    @Column(name = "cancelled")
    private Integer cancelled;

    @Column(name = "diverted")
    private Integer diverted;

    @Column(name = "ontime_percent", precision = 12, scale = 6)
    private BigDecimal ontimePercent;

    @Column(name = "delay_observations")
    private Integer delayObservations;

    @Column(name = "delay_mean", precision = 12, scale = 6)
    private BigDecimal delayMean;

    @Column(name = "delay_standard_deviation", precision = 12, scale = 6)
    private BigDecimal delayStandardDeviation;

    @Column(name = "delay_min")
    private Integer delayMin;

    @Column(name = "delay_max")
    private Integer delayMax;

    @Column(name = "all_ontime_cumulative", precision = 12, scale = 6)
    private BigDecimal allOntimeCumulative;

    @Column(name = "all_ontime_stars", precision = 12, scale = 6)
    private BigDecimal allOntimeStars;

    @Column(name = "all_delay_cumulative", precision = 12, scale = 6)
    private BigDecimal allDelayCumulative;

    @Column(name = "all_delay_stars", precision = 12, scale = 6)
    private BigDecimal allDelayStars;

    @Column(name = "all_stars", precision = 12, scale = 6)
    private BigDecimal allStars;

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;
}
