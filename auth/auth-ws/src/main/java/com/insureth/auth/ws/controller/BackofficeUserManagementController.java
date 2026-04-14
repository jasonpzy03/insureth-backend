package com.insureth.auth.ws.controller;

import com.insureth.auth.model.dto.BackofficeUserCreateRequest;
import com.insureth.auth.model.dto.BackofficeUserModel;
import com.insureth.auth.service.BackofficeUserManagementService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

    @GetMapping
    public ResponseEntity<List<BackofficeUserModel>> listUsers(Authentication authentication) {
        assertAdmin(authentication);
        return ResponseEntity.ok(backofficeUserManagementService.listUsers());
    }

    @PostMapping
    public ResponseEntity<BackofficeUserModel> createUser(
            Authentication authentication,
            @Valid @RequestBody BackofficeUserCreateRequest request,
            HttpServletRequest httpServletRequest
    ) {
        assertAdmin(authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(backofficeUserManagementService.createUser(
                request,
                authentication.getName(),
                "ADMIN",
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        ));
    }

    private void assertAdmin(Authentication authentication) {
        if (authentication == null
                || authentication.getAuthorities().stream().noneMatch(a -> a.equals(new SimpleGrantedAuthority("ROLE_ADMIN")))) {
            throw new AccessDeniedException("Admin access is required");
        }
    }
}
