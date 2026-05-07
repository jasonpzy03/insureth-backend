package com.insureth.insurance.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightStatsRatingsResponseModel {
    private FlightStatsRequestModel request;
    private List<FlightStatsRatingModel> ratings;
    private FlightStatsAppendixModel appendix;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlightStatsRequestModel {
        private String url;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlightStatsAppendixModel {
        private List<FlightStatsAirlineModel> airlines;
        private List<FlightStatsAirportModel> airports;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlightStatsAirlineModel {
        private String fs;
        private String iata;
        private String icao;
        private String name;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlightStatsAirportModel {
        private String fs;
        private String iata;
        private String icao;
        private String name;
        private String city;
        private String countryCode;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FlightStatsRatingModel {
        private String airlineFsCode;
        private String flightNumber;
        private String departureAirportFsCode;
        private String arrivalAirportFsCode;
        private Integer observations;
        private Integer ontime;
        private Integer late15;
        private Integer late30;
        private Integer late45;
        private Integer cancelled;
        private Integer diverted;
        private BigDecimal ontimePercent;
        private Integer delayObservations;
        private BigDecimal delayMean;
        private BigDecimal delayStandardDeviation;
        private Integer delayMin;
        private Integer delayMax;
        private BigDecimal allOntimeCumulative;
        private BigDecimal allOntimeStars;
        private BigDecimal allDelayCumulative;
        private BigDecimal allDelayStars;
        private BigDecimal allStars;
    }
}
