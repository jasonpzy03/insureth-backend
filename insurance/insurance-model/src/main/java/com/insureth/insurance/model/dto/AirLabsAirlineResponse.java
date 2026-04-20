package com.insureth.insurance.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class AirLabsAirlineResponse {
    @JsonProperty("response")
    private List<AirLabsAirlineModel> response;
}
