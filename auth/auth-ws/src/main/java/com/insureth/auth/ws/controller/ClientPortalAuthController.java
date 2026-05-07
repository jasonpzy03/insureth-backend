package com.insureth.auth.ws.controller;

import com.insureth.auth.model.dto.ClientPortalAuthResponse;
import com.insureth.auth.model.dto.ClientPortalLoginRequest;
import com.insureth.auth.model.dto.ClientPortalNonceRequest;
import com.insureth.auth.model.dto.ClientPortalNonceResponse;
import com.insureth.auth.model.dto.ClientUserModel;
import com.insureth.auth.service.ClientPortalAuthService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/client")
@RequiredArgsConstructor
public class ClientPortalAuthController {

    private final ClientPortalAuthService clientPortalAuthService;

    @PostMapping("/nonce")
    public ResponseEntity<ClientPortalNonceResponse> issueNonce(@Valid @RequestBody ClientPortalNonceRequest request) {
        return ResponseEntity.ok(clientPortalAuthService.issueNonce(request.getWalletAddress()));
    }

    @PostMapping("/login")
    public ResponseEntity<ClientPortalAuthResponse> login(
            @Valid @RequestBody ClientPortalLoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(clientPortalAuthService.login(
                request,
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<ClientUserModel> me(Authentication authentication) {
        return ResponseEntity.ok(clientPortalAuthService.getCurrentUser(authentication.getName()));
    }
}
