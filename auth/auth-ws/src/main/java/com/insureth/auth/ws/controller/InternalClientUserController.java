package com.insureth.auth.ws.controller;

import com.insureth.auth.model.dto.ClientUserModel;
import com.insureth.auth.service.ClientPortalUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth/internal/client-users")
@RequiredArgsConstructor
public class InternalClientUserController {

    private final ClientPortalUserService clientPortalUserService;

    @Value("${internal.api.key}")
    private String internalApiKey;

    @GetMapping
    public ResponseEntity<ClientUserModel> getClientUserByWalletAddress(
            @RequestParam("walletAddress") String walletAddress,
            @RequestHeader("X-Internal-Api-Key") String providedInternalApiKey
    ) {
        if (!internalApiKey.equals(providedInternalApiKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        return ResponseEntity.ok(clientPortalUserService.getUserByWalletAddress(walletAddress));
    }
}
