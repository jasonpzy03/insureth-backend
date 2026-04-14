package com.insureth.auth.service;

import com.insureth.auth.domain.entity.AuthAuditTrail;
import com.insureth.auth.domain.repository.AuthAuditTrailDAO;
import com.insureth.auth.domain.repository.AuthAuditTrailQuery;
import com.insureth.auth.model.dto.AdminPagedResponse;
import com.insureth.auth.model.dto.AuthAuditTrailModel;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthAuditTrailService {

    private final AuthAuditTrailDAO authAuditTrailDAO;
    private final AuthAuditTrailQuery authAuditTrailQuery;

    @Transactional
    public void record(
            String eventType,
            String actorWalletAddress,
            String actorRole,
            String targetType,
            String targetIdentifier,
            String description,
            String ipAddress,
            String userAgent
    ) {
        authAuditTrailDAO.save(AuthAuditTrail.builder()
                .eventType(eventType)
                .actorWalletAddress(actorWalletAddress)
                .actorRole(actorRole)
                .targetType(targetType)
                .targetIdentifier(targetIdentifier)
                .description(description)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(Instant.now())
                .build());
    }

    @Transactional(readOnly = true)
    public AdminPagedResponse<AuthAuditTrailModel> getAuditTrailPage(int page, int size, String search) {
        Page<AuthAuditTrail> result = authAuditTrailQuery.findAuditTrails(page, size, search);
        return AdminPagedResponse.<AuthAuditTrailModel>builder()
                .items(result.getContent().stream().map(this::toModel).toList())
                .totalElements(result.getTotalElements())
                .page(result.getNumber())
                .size(result.getSize())
                .build();
    }

    private AuthAuditTrailModel toModel(AuthAuditTrail auditTrail) {
        return AuthAuditTrailModel.builder()
                .auditId(auditTrail.getAuditId())
                .eventType(auditTrail.getEventType())
                .actorWalletAddress(auditTrail.getActorWalletAddress())
                .actorRole(auditTrail.getActorRole())
                .targetType(auditTrail.getTargetType())
                .targetIdentifier(auditTrail.getTargetIdentifier())
                .description(auditTrail.getDescription())
                .ipAddress(auditTrail.getIpAddress())
                .userAgent(auditTrail.getUserAgent())
                .createdAt(auditTrail.getCreatedAt())
                .build();
    }
}
