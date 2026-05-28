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
public class InsuranceGovernanceProposalModel {
    private Long proposalId;
    private String title;
    private String description;
    private String insuranceType;
    private String proposer;
    private Long snapshotBlock;
    private Long startBlock;
    private Long endBlock;
    private String status;
    private String forVotesEth;
    private String againstVotesEth;
    private String abstainVotesEth;
    private String quorumVotesEth;
    private boolean executed;
    private boolean cancelled;
    private Instant createdAt;
    private Instant executedAt;
    private Instant cancelledAt;
}
