package com.insureth.auth.ws.controller;

import com.insureth.auth.model.dto.ClientUserModel;
import com.insureth.auth.service.ClientPortalUserService;
import com.insureth.auth.ws.api.CpUsersApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements CpUsersApi {
    private final ClientPortalUserService clientPortalUserService;

    @Override
    public ResponseEntity<Boolean> checkClientUserExists(String walletAddress) {
        boolean exists = clientPortalUserService.checkIfUserExists(walletAddress);
        return ResponseEntity.ok(exists);
    }

    @Override
    public ResponseEntity<Void> createClientUser(ClientUserModel userModel) {
        clientPortalUserService.createUser(userModel);
        return ResponseEntity.ok().build();
    }
}

