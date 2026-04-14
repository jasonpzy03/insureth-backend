package com.insureth.insurance.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightInsuranceAdminConfigRequest {
    @NotNull
    @Valid
    private FlightInsuranceProductConfigModel product;
}
