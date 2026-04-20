package com.insureth.auth.service;

import com.insureth.auth.domain.entity.AuthNonce;
import com.insureth.auth.domain.entity.BackofficeUser;
import com.insureth.auth.domain.entity.User;
import com.insureth.auth.domain.repository.AuthNonceDAO;
import com.insureth.auth.domain.repository.BackofficeUserDAO;
import com.insureth.auth.model.dto.BackofficeAuthResponse;
import com.insureth.auth.model.dto.BackofficeLoginRequest;
import com.insureth.auth.model.dto.BackofficeNonceResponse;
import com.insureth.auth.model.dto.BackofficeUserModel;
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
public class BackofficeAuthService {

    private static final int NONCE_LENGTH = 24;
    private static final long NONCE_EXPIRY_MINUTES = 5;
    private static final char[] SIWE_NONCE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private final BackofficeUserDAO backofficeUserDAO;
    private final AuthNonceDAO authNonceDAO;
    private final JwtService jwtService;
    private final BackofficeAuthorizationService backofficeAuthorizationService;
    private final WalletSignatureVerifier walletSignatureVerifier;
    private final SiweMessageService siweMessageService;
    private final AuthAuditTrailService authAuditTrailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public BackofficeNonceResponse issueNonce(String walletAddress) {
        BackofficeUser backofficeUser = findUser(walletAddress);
        if (!backofficeUser.getUser().isActive()) {
            throw new BadCredentialsException("Backoffice account is disabled");
        }

        String nonce = generateSiweNonce();
        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plus(NONCE_EXPIRY_MINUTES, ChronoUnit.MINUTES);
        AuthNonce authNonce = authNonceDAO.save(AuthNonce.builder()
                .user(backofficeUser.getUser())
                .nonce(nonce)
                .expiresAt(expiresAt)
                .createdAt(issuedAt)
                .build());

        return BackofficeNonceResponse.builder()
                .nonce(nonce)
                .expiresAt(expiresAt)
                .message(siweMessageService.buildBackofficeMessage(backofficeUser.getUser().getWalletAddress(), nonce, authNonce.getCreatedAt()))
                .build();
    }

    @Transactional
    public BackofficeAuthResponse login(BackofficeLoginRequest request, String ipAddress, String userAgent) {
        BackofficeUser user = findUser(request.getWalletAddress());
        AuthNonce authNonce = validateNonce(user.getUser(), request.getNonce());
        Instant issuedAt = authNonce.getCreatedAt().truncatedTo(ChronoUnit.SECONDS);
        if (!walletSignatureVerifier.matchesExpectedFields(
                request.getMessage(),
                user.getUser().getWalletAddress(),
                authNonce.getNonce(),
                siweMessageService.getBackofficeDomain(),
                siweMessageService.getBackofficeUri(),
                siweMessageService.getBackofficeChainId(),
                issuedAt,
                siweMessageService.getBackofficeStatement()
        )) {
            throw new BadCredentialsException("Unexpected SIWE message payload");
        }

        if (!walletSignatureVerifier.matches(
                request.getWalletAddress(),
                request.getNonce(),
                request.getMessage(),
                request.getSignature()
        )) {
            throw new BadCredentialsException("Invalid wallet signature");
        }

        authNonce.setUsedAt(Instant.now());
        authNonceDAO.save(authNonce);

        List<String> roles = backofficeAuthorizationService.resolveRoles(user);
        List<String> rights = backofficeAuthorizationService.resolveRights(user);
        String primaryRole = backofficeAuthorizationService.resolvePrimaryRole(user);

        Instant expiresAt = jwtService.getExpiryInstant();
        String token = jwtService.generateToken(user.getUser().getWalletAddress(), primaryRole, roles, rights, user.getUserId());

        authAuditTrailService.record(
                "BACKOFFICE_LOGIN_SUCCESS",
                user.getUser().getWalletAddress(),
                primaryRole,
                "BACKOFFICE_USER",
                String.valueOf(user.getUserId()),
                "Backoffice user logged in successfully",
                ipAddress,
                userAgent
        );

        return BackofficeAuthResponse.builder()
                .token(token)
                .expiresAt(expiresAt)
                .user(toModel(user))
                .build();
    }

    @Transactional(readOnly = true)
    public BackofficeUserModel getCurrentUser(String walletAddress) {
        return toModel(findUser(walletAddress));
    }

    private BackofficeUser findUser(String walletAddress) {
        return backofficeUserDAO.findByUserWalletAddressIgnoreCase(walletAddress)
                .orElseThrow(() -> new BadCredentialsException("Backoffice wallet is not authorized"));
    }

    private AuthNonce validateNonce(User user, String nonce) {
        AuthNonce authNonce = authNonceDAO.findFirstByUserUserIdAndNonceAndUsedAtIsNullOrderByCreatedAtDesc(user.getUserId(), nonce)
                .orElseThrow(() -> new BadCredentialsException("Login nonce missing. Request a fresh nonce."));

        if (authNonce.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Login nonce expired");
        }

        return authNonce;
    }

    private BackofficeUserModel toModel(BackofficeUser user) {
        List<String> roles = backofficeAuthorizationService.resolveRoles(user);
        List<String> rights = backofficeAuthorizationService.resolveRights(user);
        return BackofficeUserModel.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .walletAddress(user.getUser().getWalletAddress())
                .role(backofficeAuthorizationService.resolvePrimaryRole(user))
                .roles(roles)
                .rights(rights)
                .build();
    }

    private String generateSiweNonce() {
        StringBuilder builder = new StringBuilder(NONCE_LENGTH);
        for (int i = 0; i < NONCE_LENGTH; i++) {
            builder.append(SIWE_NONCE_CHARS[secureRandom.nextInt(SIWE_NONCE_CHARS.length)]);
        }
        return builder.toString();
    }
}
