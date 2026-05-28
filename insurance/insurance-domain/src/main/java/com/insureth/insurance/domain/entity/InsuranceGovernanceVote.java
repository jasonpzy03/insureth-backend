package com.insureth.insurance.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
        name = "insurance_governance_vote",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_insurance_governance_vote_event_key", columnNames = {"event_key"}),
                @UniqueConstraint(name = "uk_insurance_governance_vote_proposal_voter", columnNames = {"proposal_id", "voter"})
        },
        indexes = {
                @Index(name = "idx_insurance_governance_vote_proposal", columnList = "proposal_id"),
                @Index(name = "idx_insurance_governance_vote_voter", columnList = "voter")
        }
)
public class InsuranceGovernanceVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    private Long voteId;

    @Column(name = "event_key", nullable = false, length = 160)
    private String eventKey;

    @Column(name = "proposal_id", nullable = false)
    private Long proposalId;

    @Column(name = "voter", nullable = false, length = 100)
    private String voter;

    @Column(name = "support", nullable = false, length = 30)
    private String support;

    @Column(name = "weight_wei", nullable = false, length = 120)
    private String weightWei;

    @Column(name = "transaction_hash", nullable = false, length = 66)
    private String transactionHash;

    @Column(name = "block_number", nullable = false)
    private Long blockNumber;

    @Column(name = "log_index", nullable = false)
    private Integer logIndex;

    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
