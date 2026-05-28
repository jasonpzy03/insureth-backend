package com.insureth.insurance.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "insurance_governance_proposal",
        indexes = {
                @Index(name = "idx_insurance_governance_proposal_status", columnList = "status"),
                @Index(name = "idx_insurance_governance_proposal_end_block", columnList = "end_block")
        }
)
public class InsuranceGovernanceProposal {

    @Id
    @Column(name = "proposal_id")
    private Long proposalId;

    @Column(name = "proposer", nullable = false, length = 100)
    private String proposer;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "insurance_type", nullable = false, length = 120)
    private String insuranceType;

    @Column(name = "snapshot_block", nullable = false)
    private Long snapshotBlock;

    @Column(name = "start_block", nullable = false)
    private Long startBlock;

    @Column(name = "end_block", nullable = false)
    private Long endBlock;

    @Column(name = "quorum_votes_wei", nullable = false, length = 120)
    private String quorumVotesWei;

    @Column(name = "for_votes_wei", nullable = false, length = 120)
    private String forVotesWei;

    @Column(name = "against_votes_wei", nullable = false, length = 120)
    private String againstVotesWei;

    @Column(name = "abstain_votes_wei", nullable = false, length = 120)
    private String abstainVotesWei;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "executed", nullable = false)
    private boolean executed;

    @Column(name = "cancelled", nullable = false)
    private boolean cancelled;

    @Column(name = "transaction_hash", nullable = false, length = 66)
    private String transactionHash;

    @Column(name = "block_number", nullable = false)
    private Long blockNumber;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "executed_at")
    private Instant executedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;
}
