package com.insureth.insurance.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightInsuranceAdminPolicySyncRequest {
    @Valid
    @NotNull
    private List<FlightInsuranceAdminPolicyRecordModel> policies;
}
