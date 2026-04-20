package com.insureth.insurance.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AirLabsAirportModel {
    @JsonProperty("name")
    private String name;

    @JsonProperty("iata_code")
    private String iataCode;

    @JsonProperty("icao_code")
    private String icaoCode;

    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("lat")
    private Double lat;

    @JsonProperty("lng")
    private Double lng;

    @JsonProperty("country_code")
    private String countryCode;
}
