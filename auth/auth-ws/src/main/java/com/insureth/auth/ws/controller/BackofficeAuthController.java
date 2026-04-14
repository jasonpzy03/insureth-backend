package com.insureth.auth.ws.controller;

import com.insureth.auth.model.dto.BackofficeAuthResponse;
import com.insureth.auth.model.dto.BackofficeLoginRequest;
import com.insureth.auth.model.dto.BackofficeNonceRequest;
import com.insureth.auth.model.dto.BackofficeNonceResponse;
import com.insureth.auth.model.dto.BackofficeUserModel;
import com.insureth.auth.service.BackofficeAuthService;
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
@RequestMapping("/api/v1/auth/backoffice")
@RequiredArgsConstructor
public class BackofficeAuthController {

    private final BackofficeAuthService backofficeAuthService;

    @PostMapping("/nonce")
    public ResponseEntity<BackofficeNonceResponse> issueNonce(@Valid @RequestBody BackofficeNonceRequest request) {
        return ResponseEntity.ok(backofficeAuthService.issueNonce(request.getWalletAddress()));
    }

    @PostMapping("/login")
    public ResponseEntity<BackofficeAuthResponse> login(
            @Valid @RequestBody BackofficeLoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return ResponseEntity.ok(backofficeAuthService.login(
                request,
                httpServletRequest.getRemoteAddr(),
                httpServletRequest.getHeader("User-Agent")
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<BackofficeUserModel> me(Authentication authentication) {
        return ResponseEntity.ok(backofficeAuthService.getCurrentUser(authentication.getName()));
    }
}
