package com.insureth.auth.model.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthAuditTrailModel {
    private Long auditId;
    private String eventType;
    private String actorWalletAddress;
    private String actorRole;
    private String targetType;
    private String targetIdentifier;
    private String description;
    private String ipAddress;
    private String userAgent;
    private Instant createdAt;
}
