package com.insureth.insurance.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
@Table(name = "flight_insurance_policy_nft_sync_state")
public class FlightInsurancePolicyNftSyncState {

    @Id
    @Column(name = "policy_id", nullable = false)
    private Long policyId;

    @Column(name = "holder", nullable = false, length = 100)
    private String holder;

    @Column(name = "risk_key", nullable = false, length = 66)
    private String riskKey;

    @Column(name = "metadata_cid", length = 255)
    private String metadataCid;

    @Column(name = "token_uri", length = 500)
    private String tokenUri;

    @Column(name = "contract_update_tx_hash", length = 100)
    private String contractUpdateTxHash;

    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
