package com.insureth.insurance.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AirLabsScheduleResponse {
    @JsonProperty("response")
    private List<AirLabsScheduleModel> response;
}
