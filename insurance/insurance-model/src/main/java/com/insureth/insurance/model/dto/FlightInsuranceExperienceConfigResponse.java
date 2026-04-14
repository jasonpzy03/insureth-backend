package com.insureth.insurance.model.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightInsuranceExperienceConfigResponse {
    private FlightInsuranceProductConfigModel product;
    private List<AirlineResponseModel> supportedAirlines;
    private List<AirportResponseModel> supportedAirports;
}
