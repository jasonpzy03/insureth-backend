package com.insureth.insurance.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightInsuranceAirlineUpdateRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String iataCode;

    @NotBlank
    private String icaoCode;

    @NotNull
    private Boolean supportedForFlightInsurance;
}
