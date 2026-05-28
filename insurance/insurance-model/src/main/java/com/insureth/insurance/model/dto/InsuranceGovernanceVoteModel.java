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
public class InsuranceGovernanceVoteModel {
    private String voter;
    private String support;
    private String weightEth;
    private Instant timestamp;
    private String transactionHash;
}
