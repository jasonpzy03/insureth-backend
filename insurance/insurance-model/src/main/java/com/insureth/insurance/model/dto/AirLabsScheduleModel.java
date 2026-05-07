package com.insureth.insurance.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AirLabsScheduleModel {
    @JsonProperty("flight_iata")
    private String flightIata;

    @JsonProperty("dep_iata")
    private String depIata;

    @JsonProperty("arr_iata")
    private String arrIata;

    @JsonProperty("dep_time")
    private String depTime;

    @JsonProperty("arr_time")
    private String arrTime;

    @JsonProperty("dep_time_utc")
    private String depTimeUtc;

    @JsonProperty("arr_time_utc")
    private String arrTimeUtc;

    @JsonProperty("dep_time_ts")
    private Long depTimeTs;

    @JsonProperty("arr_time_ts")
    private Long arrTimeTs;

    @JsonProperty("status")
    private String status;
}
