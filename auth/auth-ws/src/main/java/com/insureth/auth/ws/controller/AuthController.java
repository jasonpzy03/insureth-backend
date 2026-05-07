package com.insureth.auth.ws.controller;

import com.insureth.auth.model.dto.ClientUserModel;
import com.insureth.auth.model.dto.VerifySignupRequest;
import com.insureth.auth.service.ClientPortalUserService;
import com.insureth.auth.ws.api.CpUsersApi;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController implements CpUsersApi {
    private final ClientPortalUserService clientPortalUserService;
    private final AuthenticationFacade authenticationFacade;

    @Override
    public ResponseEntity<Boolean> checkClientUserExists(String walletAddress) {
        boolean exists = clientPortalUserService.checkIfUserExists(walletAddress);
        return ResponseEntity.ok(exists);
    }

    @Override
    public ResponseEntity<Void> createClientUser(ClientUserModel userModel) {
        authenticationFacade.requireWalletMatch(userModel.getWalletAddress());
        clientPortalUserService.createUser(userModel);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<ClientUserModel> getClientUser(String walletAddress) {
        authenticationFacade.requireWalletMatch(walletAddress);
        ClientUserModel user = clientPortalUserService.getUserByWalletAddress(walletAddress);
        return ResponseEntity.ok(user);
    }

    @Override
    public ResponseEntity<Void> updateClientUser(String walletAddress, ClientUserModel userModel) {
        authenticationFacade.requireWalletMatch(walletAddress);
        clientPortalUserService.updateUser(walletAddress, userModel);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> initiateSignup(ClientUserModel userModel) {
        authenticationFacade.requireWalletMatch(userModel.getWalletAddress());
        clientPortalUserService.initiateSignup(userModel);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> verifySignup(VerifySignupRequest verifySignupRequest) {
        clientPortalUserService.verifySignup(verifySignupRequest.getToken());
        return ResponseEntity.ok().build();
    }
}

