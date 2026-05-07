package com.insureth.auth.ws.controller;

import com.insureth.auth.model.dto.AdminPagedResponse;
import com.insureth.auth.model.dto.AuthAuditTrailModel;
import com.insureth.auth.service.BackofficeRight;
import com.insureth.auth.service.AuthAuditTrailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/backoffice/audit-trail")
@RequiredArgsConstructor
public class AuthAuditTrailController {

    private final AuthAuditTrailService authAuditTrailService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + BackofficeRight.AUTH_AUDIT_VIEW + "')")
    public ResponseEntity<AdminPagedResponse<AuthAuditTrailModel>> getAuditTrails(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(authAuditTrailService.getAuditTrailPage(page, size, search));
    }
}
