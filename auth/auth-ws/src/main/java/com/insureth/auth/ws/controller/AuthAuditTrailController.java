package com.insureth.auth.ws.controller;

import com.insureth.auth.model.dto.AdminPagedResponse;
import com.insureth.auth.model.dto.AuthAuditTrailModel;
import com.insureth.auth.service.AuthAuditTrailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
    public ResponseEntity<AdminPagedResponse<AuthAuditTrailModel>> getAuditTrails(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search
    ) {
        assertAdmin(authentication);
        return ResponseEntity.ok(authAuditTrailService.getAuditTrailPage(page, size, search));
    }

    private void assertAdmin(Authentication authentication) {
        if (authentication == null
                || authentication.getAuthorities().stream().noneMatch(a -> a.equals(new SimpleGrantedAuthority("ROLE_ADMIN")))) {
            throw new AccessDeniedException("Admin access is required");
        }
    }
}
