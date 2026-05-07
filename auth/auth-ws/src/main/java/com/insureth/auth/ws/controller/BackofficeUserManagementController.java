package com.insureth.auth.ws.controller;

import com.insureth.auth.model.dto.BackofficeUserCreateRequest;
import com.insureth.auth.model.dto.BackofficeRoleModel;
import com.insureth.auth.model.dto.BackofficeUserModel;
import com.insureth.auth.service.BackofficeAuthorizationService;
import com.insureth.auth.service.BackofficeRight;
import com.insureth.auth.service.BackofficeUserManagementService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/backoffice/users")
@RequiredArgsConstructor
public class BackofficeUserManagementController {

    private final BackofficeUserManagementService backofficeUserManagementService;
    private final BackofficeAuthorizationService backofficeAuthorizationService;

    @GetMapping
    @PreAuthorize("hasAuthority('" + BackofficeRight.BACKOFFICE_USER_VIEW + "')")
    public ResponseEntity<List<BackofficeUserModel>> listUsers() {
        return ResponseEntity.ok(backofficeUserManagementService.listUsers());
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('" + BackofficeRight.BACKOFFICE_USER_VIEW + "')")
    public ResponseEntity<List<BackofficeRoleModel>> listRoles() {
        return ResponseEntity.ok(backofficeUserManagementService.listRoles());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + BackofficeRight.BACKOFFICE_USER_CREATE + "')")
    public ResponseEntity<BackofficeUserModel> createUser(
            Authentication authentication,
            @Valid @RequestBody BackofficeUserCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(backofficeUserManagementService.createUser(
                request,
                authentication.getName(),
                backofficeAuthorizationService.resolvePrimaryRoleFromAuthorities(authentication.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .toList()),
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        ));
    }
}
