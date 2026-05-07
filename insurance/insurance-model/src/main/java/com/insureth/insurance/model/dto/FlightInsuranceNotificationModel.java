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
public class FlightInsuranceNotificationModel {
    private String id;
    private Long policyId;
    private String type;
    private String title;
    private String message;
    private Instant timestamp;
    private String amountEth;
}
