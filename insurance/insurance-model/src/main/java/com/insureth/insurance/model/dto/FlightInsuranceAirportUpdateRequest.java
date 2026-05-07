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
public class FlightInsuranceAirportUpdateRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String iataCode;

    @NotBlank
    private String icaoCode;

    private String timezone;

    @NotNull
    private Boolean supportedForFlightInsurance;
}
