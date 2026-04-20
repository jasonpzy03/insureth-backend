package com.insureth.insurance.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class AirLabsAirportResponse {
    @JsonProperty("response")
    private List<AirLabsAirportModel> response;
}
