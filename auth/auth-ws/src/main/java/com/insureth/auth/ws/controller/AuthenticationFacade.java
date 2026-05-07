package com.insureth.auth.ws.controller;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade {

    public void requireWalletMatch(String walletAddress) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new AccessDeniedException("Authentication required");
        }

        if (!authentication.getName().equalsIgnoreCase(walletAddress)) {
            throw new AccessDeniedException("Wallet does not match authenticated session");
        }
    }
}
