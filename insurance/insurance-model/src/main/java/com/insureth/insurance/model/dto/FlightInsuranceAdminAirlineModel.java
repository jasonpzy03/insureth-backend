package com.insureth.insurance.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightInsuranceAdminAirlineModel {
    private Long airlineId;
    private String name;
    private String iataCode;
    private String icaoCode;
    private boolean supportedForFlightInsurance;
}
