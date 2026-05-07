package com.insureth.auth.service;

import com.insureth.auth.domain.entity.AuthNonce;
import com.insureth.auth.domain.entity.User;
import com.insureth.auth.domain.repository.AuthNonceDAO;
import com.insureth.auth.domain.repository.ClientPortalUserDAO;
import com.insureth.auth.domain.repository.UserDAO;
import com.insureth.auth.model.dto.ClientPortalAuthResponse;
import com.insureth.auth.model.dto.ClientPortalLoginRequest;
import com.insureth.auth.model.dto.ClientPortalNonceResponse;
import com.insureth.auth.model.dto.ClientUserModel;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClientPortalAuthService {

    private static final int NONCE_LENGTH = 24;
    private static final long NONCE_EXPIRY_MINUTES = 5;
    private static final char[] SIWE_NONCE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private final UserDAO userDAO;
    private final ClientPortalUserDAO clientPortalUserDAO;
    private final AuthNonceDAO authNonceDAO;
    private final JwtService jwtService;
    private final WalletSignatureVerifier walletSignatureVerifier;
    private final SiweMessageService siweMessageService;
    private final AuthAuditTrailService authAuditTrailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public ClientPortalNonceResponse issueNonce(String walletAddress) {
        User user = userDAO.findByWalletAddressIgnoreCase(walletAddress)
                .orElseGet(() -> userDAO.save(User.builder()
                        .walletAddress(walletAddress)
                        .active(true)
                        .build()));

        if (!user.isActive()) {
            throw new BadCredentialsException("Client portal account is disabled");
        }

        String nonce = generateSiweNonce();
        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plus(NONCE_EXPIRY_MINUTES, ChronoUnit.MINUTES);
        AuthNonce authNonce = authNonceDAO.save(AuthNonce.builder()
                .user(user)
                .nonce(nonce)
                .expiresAt(expiresAt)
                .createdAt(issuedAt)
                .build());

        return ClientPortalNonceResponse.builder()
                .nonce(nonce)
                .expiresAt(expiresAt)
                .message(siweMessageService.buildClientPortalMessage(user.getWalletAddress(), nonce, authNonce.getCreatedAt()))
                .build();
    }

    @Transactional
    public ClientPortalAuthResponse login(ClientPortalLoginRequest request, String ipAddress, String userAgent) {
        User user = userDAO.findByWalletAddressIgnoreCase(request.getWalletAddress())
                .orElseThrow(() -> new BadCredentialsException("Wallet identity not found"));

        AuthNonce authNonce = validateNonce(user, request.getNonce());
        Instant issuedAt = authNonce.getCreatedAt().truncatedTo(ChronoUnit.SECONDS);
        if (!walletSignatureVerifier.matchesExpectedFields(
                request.getMessage(),
                user.getWalletAddress(),
                authNonce.getNonce(),
                siweMessageService.getClientPortalDomain(),
                siweMessageService.getClientPortalUri(),
                siweMessageService.getClientPortalChainId(),
                issuedAt,
                siweMessageService.getClientPortalStatement()
        )) {
            throw new BadCredentialsException("Unexpected SIWE message payload");
        }

        if (!walletSignatureVerifier.matchesClientPortal(
                request.getWalletAddress(),
                request.getNonce(),
                request.getMessage(),
                request.getSignature()
        )) {
            throw new BadCredentialsException("Invalid wallet signature");
        }

        authNonce.setUsedAt(Instant.now());
        authNonceDAO.save(authNonce);

        boolean profileComplete = clientPortalUserDAO.existsByUserWalletAddressIgnoreCase(request.getWalletAddress());
        Instant expiresAt = jwtService.getExpiryInstant();
        String token = jwtService.generateToken(
                user.getWalletAddress(),
                "CLIENT_PORTAL",
                List.of("CLIENT_PORTAL"),
                List.of(),
                user.getUserId()
        );

        authAuditTrailService.record(
                "CLIENT_PORTAL_LOGIN_SUCCESS",
                user.getWalletAddress(),
                "CLIENT_PORTAL",
                "CLIENT_IDENTITY",
                String.valueOf(user.getUserId()),
                "Client portal user logged in successfully",
                ipAddress,
                userAgent
        );

        return ClientPortalAuthResponse.builder()
                .token(token)
                .expiresAt(expiresAt)
                .profileComplete(profileComplete)
                .walletAddress(user.getWalletAddress())
                .build();
    }

    public ClientUserModel getCurrentUser(String walletAddress) {
        var user = clientPortalUserDAO.findByUserWalletAddressIgnoreCase(walletAddress)
                .orElseThrow(() -> new BadCredentialsException("Client profile not found"));

        ClientUserModel model = new ClientUserModel();
        model.setWalletAddress(user.getUser().getWalletAddress());
        model.setUsername(user.getUsername());
        model.setEmail(user.getEmail());
        return model;
    }

    private AuthNonce validateNonce(User user, String nonce) {
        AuthNonce authNonce = authNonceDAO.findFirstByUserUserIdAndNonceAndUsedAtIsNullOrderByCreatedAtDesc(user.getUserId(), nonce)
                .orElseThrow(() -> new BadCredentialsException("Login nonce missing. Request a fresh nonce."));

        if (authNonce.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Login nonce expired");
        }

        return authNonce;
    }

    private String generateSiweNonce() {
        StringBuilder builder = new StringBuilder(NONCE_LENGTH);
        for (int i = 0; i < NONCE_LENGTH; i++) {
            builder.append(SIWE_NONCE_CHARS[secureRandom.nextInt(SIWE_NONCE_CHARS.length)]);
        }
        return builder.toString();
    }
}
