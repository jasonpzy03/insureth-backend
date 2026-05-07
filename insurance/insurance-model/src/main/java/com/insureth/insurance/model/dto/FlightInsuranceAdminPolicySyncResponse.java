package com.insureth.insurance.model.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightInsuranceAdminPolicySyncResponse {
    private int syncedPolicies;
    private Instant syncedAt;
}
