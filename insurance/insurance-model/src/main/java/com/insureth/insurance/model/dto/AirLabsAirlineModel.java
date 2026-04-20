package com.insureth.insurance.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AirLabsAirlineModel {
    @JsonProperty("name")
    private String name;

    @JsonProperty("iata_code")
    private String iataCode;

    @JsonProperty("icao_code")
    private String icaoCode;
}
