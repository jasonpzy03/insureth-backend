package com.insureth.insurance.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightInsuranceAdminConfigResponse {
    private FlightInsuranceProductConfigModel product;
}
