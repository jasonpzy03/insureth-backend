package com.insureth.insurance.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "insurance_audit_trail")
public class InsuranceAuditTrail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "actor_wallet_address")
    private String actorWalletAddress;

    @Column(name = "actor_role")
    private String actorRole;

    @Column(name = "target_type")
    private String targetType;

    @Column(name = "target_identifier")
    private String targetIdentifier;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", length = 1000)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
